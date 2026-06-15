package com.cafetron.order.service;

import com.cafetron.cart.entity.OrderItem;
import com.cafetron.cart.repository.OrderItemRepository;
import com.cafetron.menu.entity.MenuItem;
import com.cafetron.menu.repository.MenuItemRepository;
import com.cafetron.order.dto.*;
import com.cafetron.order.entity.Order;
import com.cafetron.order.repository.OrderRepository;
import com.cafetron.orderQR.service.OrderQRService;
import com.cafetron.pickup.VendorOrderStatus;
import com.cafetron.pickup.VendorOrderStatusType;
import com.cafetron.pickup.repository.VendorOrderStatusRepository;
import com.cafetron.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WalletService walletService;
    private final OrderQRService orderQRService;
    private final VendorOrderStatusRepository vendorOrderStatusRepository;

    public OrderServiceImpl(
            MenuItemRepository menuItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            WalletService walletService,
            OrderQRService orderQRService,
            VendorOrderStatusRepository vendorOrderStatusRepository
    ) {
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.walletService = walletService;
        this.orderQRService = orderQRService;
        this.vendorOrderStatusRepository = vendorOrderStatusRepository;
    }

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        // Slice 3: sort items by menuItemId ascending to guarantee consistent lock order
        // prevents deadlock when two concurrent requests share some menu items
        List<PlaceOrderItemRequest> sortedItems = request.items().stream()
                .sorted(Comparator.comparing(PlaceOrderItemRequest::menuItemId))
                .toList();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Set<Long> uniqueVendorIds = new HashSet<>();

        Order order = new Order();
        order.setUserId(userId);
        order.setPickupSlot(request.pickupSlot());
        order.setOverallStatus("PLACED");
        order.setPaymentStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        for (PlaceOrderItemRequest itemRequest : sortedItems) {
            MenuItem menuItem = menuItemRepository.findByIdForUpdate(itemRequest.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemRequest.menuItemId()));

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item is not available: " + menuItem.getItemName());
            }
            if (menuItem.getStock() < itemRequest.quantity()) {
                throw new IllegalStateException("Insufficient stock for item: " + menuItem.getItemName());
            }

            menuItem.setStock(menuItem.getStock() - itemRequest.quantity());
            if (menuItem.getStock() == 0) {
                menuItem.setAvailable(false);
            }

            BigDecimal unitPrice = BigDecimal.valueOf(menuItem.getPrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            totalAmount = totalAmount.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(unitPrice);
            orderItems.add(orderItem);

            if (menuItem.getVendor() != null && menuItem.getVendor().getId() != null) {
                uniqueVendorIds.add(menuItem.getVendor().getId());
            }
        }

        order.setTotalAmount(totalAmount);
        order.setVendorCount(uniqueVendorIds.size());
        order.setVendorAcceptedCount(0);

        walletService.debit(userId, totalAmount, "Order placement");

        Order savedOrder = orderRepository.save(order);

        // assign saved order back to each item before saving
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);

        // Slice 4a: create one VendorOrderStatus row per order item (PENDING, 30-min window)
        List<VendorOrderStatus> vendorStatuses = new ArrayList<>();
        for (OrderItem savedItem : savedItems) {
            VendorOrderStatus vs = new VendorOrderStatus();
            vs.setOrderItem(savedItem);
            vs.setVendor(savedItem.getMenuItem().getVendor());
            vs.setStatus(VendorOrderStatusType.PENDING);
            vs.setActionExpiresAt(LocalDateTime.now().plusMinutes(30));
            vs.setCreatedAt(LocalDateTime.now());
            vendorStatuses.add(vs);
        }
        vendorOrderStatusRepository.saveAll(vendorStatuses);

        // Slice 4b: generate real QR token via OrderQRService (stores base64 QR in OrderQR table)
        String token = orderQRService.generateAndStoreQR(savedOrder);
        savedOrder.setToken(token);
        orderRepository.save(savedOrder);

        return new PlaceOrderResponse(
                savedOrder.getId(),
                savedOrder.getOverallStatus(),
                savedOrder.getPaymentStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getToken()
        );
    }

    @Override
    public List<MyOrderSummaryResponse> getMyOrders(Long userId){
        List <Order> allOrders =  orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<MyOrderSummaryResponse> myOrders = new ArrayList<>();
        for (Order order : allOrders) {
            MyOrderSummaryResponse dto = new MyOrderSummaryResponse(
                    order.getId(),
                    order.getOverallStatus(),
                    order.getPaymentStatus(),
                    order.getTotalAmount(),
                    order.getPickupSlot(),
                    order.getLocation(),
                    order.getCreatedAt()
            );
            myOrders.add(dto);
        }
        return myOrders;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        // 1. fetch order, 404 if not found
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // 2. ownership check — reject if order belongs to a different user
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Access denied: order does not belong to this user.");
        }

         // 3. fetch all order items for this order with menuItem eagerly loaded via join to prevent N+1
         List<OrderItem> orderItems = orderItemRepository.findByOrder_IdWithMenuItems(orderId);

         // fetch vendor status rows and index by orderItem id for O(1) lookup during mapping
         Map<Long, VendorOrderStatusType> statusByOrderItemId = new HashMap<>();
         for (VendorOrderStatus vendorStatus : vendorOrderStatusRepository.findByOrderItem_Order_IdWithOrderItem(orderId)) {
             statusByOrderItemId.put(vendorStatus.getOrderItem().getId(), vendorStatus.getStatus());
         }

        // 4. map each OrderItem -> OrderDetailItemResponse
        List<OrderDetailItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem oi : orderItems) {
            VendorOrderStatusType vendorStatus = statusByOrderItemId.getOrDefault(oi.getId(), VendorOrderStatusType.PENDING);
            itemResponses.add(new OrderDetailItemResponse(
                    oi.getMenuItem().getId(),
                    oi.getMenuItem().getItemName(),
                    oi.getQuantity(),
                    oi.getUnitPrice(),
                    vendorStatus.name()
            ));
        }

        // 5. assemble top-level response
        return new OrderDetailResponse(
                order.getId(),
                order.getOverallStatus(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                order.getPickupSlot(),
                order.getLocation(),
                order.getToken(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    @Override
    @Transactional
    public OrderDetailResponse processTimeout(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Access denied: order does not belong to this user.");
        }

         LocalDateTime now = LocalDateTime.now();
         List<VendorOrderStatus> statuses = vendorOrderStatusRepository.findByOrderItem_Order_IdWithOrderItem(orderId);

        boolean timeoutApplied = false;
        for (VendorOrderStatus status : statuses) {
            boolean isPending = status.getStatus() == VendorOrderStatusType.PENDING;
            boolean isExpired = status.getActionExpiresAt() != null && !status.getActionExpiresAt().isAfter(now);
            if (isPending && isExpired) {
                status.setStatus(VendorOrderStatusType.TIMEOUT);
                status.setActionedAt(now);
                timeoutApplied = true;
            }
        }

        if (timeoutApplied) {
            vendorOrderStatusRepository.saveAll(statuses);

            if (!"REFUNDED".equalsIgnoreCase(order.getPaymentStatus())) {
                walletService.refund(userId, order.getTotalAmount(), "Order timeout refund");
            }

            order.setOverallStatus("TIMEOUT");
            order.setPaymentStatus("REFUNDED");
            orderRepository.save(order);
        }

        return getOrderDetail(userId, orderId);
    }
}
