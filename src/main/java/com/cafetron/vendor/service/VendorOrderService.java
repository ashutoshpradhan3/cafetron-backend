package com.cafetron.vendor.service;

import com.cafetron.cart.entity.OrderItem;
import com.cafetron.order.entity.Order;
import com.cafetron.order.repository.OrderRepository;
import com.cafetron.pickup.VendorOrderStatus;
import com.cafetron.pickup.VendorOrderStatusType;
import com.cafetron.pickup.repository.VendorOrderStatusRepository;
import com.cafetron.security.UserPrincipal;
import com.cafetron.vendor.dto.VendorOrderResponse;
import com.cafetron.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class VendorOrderService {
    private static final String ROLE_VENDOR = "VENDOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private final VendorOrderStatusRepository vendorOrderStatusRepository;
    private final OrderRepository orderRepository;
    private final WalletService walletService;

    public VendorOrderService(
            VendorOrderStatusRepository vendorOrderStatusRepository,
            OrderRepository orderRepository,
            WalletService walletService
    ) {
        this.vendorOrderStatusRepository = vendorOrderStatusRepository;
        this.orderRepository = orderRepository;
        this.walletService = walletService;
    }

    @Transactional(readOnly = true)
    public List<VendorOrderResponse> getMyVendorOrders(UserPrincipal principal) {
        String vendorEmail = getVendorEmail(principal);
        return vendorOrderStatusRepository.findVendorQueue(vendorEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VendorOrderResponse accept(UserPrincipal principal, Long statusId) {
        VendorOrderStatus status = getOwnedStatus(principal, statusId);
        requirePending(status);

        Order order = status.getOrderItem().getOrder();
        requireOrderActionable(order);

        status.setStatus(VendorOrderStatusType.ACCEPTED);
        status.setActionedAt(LocalDateTime.now());
        status.setDeclinedReason(null);
        vendorOrderStatusRepository.save(status);

        int acceptedCount = order.getVendorAcceptedCount() == null ? 0 : order.getVendorAcceptedCount();
        order.setVendorAcceptedCount(acceptedCount + 1);
        if (order.getVendorCount() != null && order.getVendorAcceptedCount() >= order.getVendorCount()) {
            order.setOverallStatus("VENDOR_ACCEPTED");
        } else {
            order.setOverallStatus("PENDING_VENDOR");
        }
        orderRepository.save(order);

        return toResponse(status);
    }

    @Transactional
    public VendorOrderResponse decline(UserPrincipal principal, Long statusId, String reason) {
        VendorOrderStatus status = getOwnedStatus(principal, statusId);
        requirePending(status);

        Order order = status.getOrderItem().getOrder();
        requireOrderActionable(order);

        status.setStatus(VendorOrderStatusType.DECLINED);
        status.setDeclinedReason(reason == null || reason.isBlank() ? "Declined by vendor" : reason.trim());
        status.setActionedAt(LocalDateTime.now());
        vendorOrderStatusRepository.save(status);

        order.setOverallStatus("VENDOR_DECLINED");
        refundOrderIfNeeded(order, "Order declined by vendor");
        orderRepository.save(order);

        return toResponse(status);
    }

    private void refundOrderIfNeeded(Order order, String description) {
        if (!"REFUNDED".equalsIgnoreCase(order.getPaymentStatus())) {
            walletService.refund(order.getUserId(), order.getTotalAmount(), description);
            order.setPaymentStatus("REFUNDED");
        }
    }

    private VendorOrderStatus getOwnedStatus(UserPrincipal principal, Long statusId) {
        return vendorOrderStatusRepository.findOwnedStatus(statusId, getVendorEmail(principal))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor order not found"));
    }

    private String getVendorEmail(UserPrincipal principal) {
        requireVendorOrAdmin(principal);
        String email = principal.getUser().getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vendor email is not available");
        }
        return email;
    }

    private void requireVendorOrAdmin(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String role = principal.getRole() == null
                ? ""
                : principal.getRole().trim().toUpperCase(Locale.ROOT);
        if (!ROLE_VENDOR.equals(role) && !ROLE_ADMIN.equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vendor access required");
        }
    }

    private void requirePending(VendorOrderStatus status) {
        if (status.getStatus() != VendorOrderStatusType.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This order has already been actioned");
        }
    }

    private void requireOrderActionable(Order order) {
        String orderStatus = order.getOverallStatus() == null ? "" : order.getOverallStatus();
        boolean terminalOrder = "VENDOR_DECLINED".equalsIgnoreCase(orderStatus)
                || "TIMEOUT".equalsIgnoreCase(orderStatus)
                || "CANCELLED".equalsIgnoreCase(orderStatus);
        boolean refunded = "REFUNDED".equalsIgnoreCase(order.getPaymentStatus());

        if (terminalOrder || refunded) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This order is already closed");
        }
    }

    private VendorOrderResponse toResponse(VendorOrderStatus status) {
        OrderItem item = status.getOrderItem();
        Order order = item.getOrder();
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new VendorOrderResponse(
                status.getId(),
                order.getId(),
                item.getId(),
                item.getMenuItem().getId(),
                item.getMenuItem().getItemName(),
                item.getQuantity(),
                unitPrice,
                lineTotal,
                order.getPickupSlot(),
                order.getOverallStatus(),
                status.getStatus().name(),
                status.getDeclinedReason(),
                status.getActionExpiresAt(),
                order.getCreatedAt()
        );
    }
}
