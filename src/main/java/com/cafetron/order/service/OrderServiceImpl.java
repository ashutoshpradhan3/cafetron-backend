package com.cafetron.order.service;

import com.cafetron.cart.entity.OrderItem;
import com.cafetron.cart.repository.OrderItemRepository;
import com.cafetron.menu.entity.MenuItem;
import com.cafetron.menu.repository.MenuItemRepository;
import com.cafetron.order.dto.PlaceOrderItemRequest;
import com.cafetron.order.dto.PlaceOrderRequest;
import com.cafetron.order.dto.PlaceOrderResponse;
import com.cafetron.order.entity.Order;
import com.cafetron.order.repository.OrderRepository;
import com.cafetron.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WalletService walletService;

    public OrderServiceImpl(
            MenuItemRepository menuItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            WalletService walletService
    ) {
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.walletService = walletService;
    }

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        // Store the details after deconstructing the request
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Set<Long> uniqueVendorIds = new HashSet<>();

        //Populate the order obj
        Order order = new Order();
        order.setUserId(userId);
        // Current Order entity does not have pickupSlot/userId yet, so pickupSlot is stored in location.
        order.setLocation(request.pickupSlot());
        order.setOverallStatus("PLACED");
        order.setPaymentStatus("PAID");
        order.setToken(UUID.randomUUID().toString());
        order.setCreatedAt(LocalDateTime.now());

        for (PlaceOrderItemRequest itemRequest : request.items()) {
            if (itemRequest.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0.");
            }

            //using optional enables us to handle null checks safely
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

            //Basic calc for deciding the bill
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

        walletService.debit(userId,totalAmount,"Order placement");

        Order savedOrder = orderRepository.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
        }
        orderItemRepository.saveAll(orderItems);

        return new PlaceOrderResponse(
                savedOrder.getId(),
                savedOrder.getOverallStatus(),
                savedOrder.getPaymentStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getToken()
        );
    }
}
