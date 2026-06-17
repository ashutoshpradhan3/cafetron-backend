package com.cafetron.order.controller;

import com.cafetron.order.dto.MyOrderSummaryResponse;
import com.cafetron.order.dto.OrderDetailResponse;
import com.cafetron.order.dto.PlaceOrderRequest;
import com.cafetron.order.dto.PlaceOrderResponse;
import com.cafetron.order.service.OrderService;
import com.cafetron.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceOrderResponse placeOrder(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PlaceOrderRequest request) {

        if ( principal == null ) {
            throw new IllegalStateException("User is not authenticated");
        }

        return orderService.placeOrder(principal.getId(), request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MyOrderSummaryResponse> getMyOrderSummary(@AuthenticationPrincipal UserPrincipal principal){
        if ( principal == null ) {
            throw new IllegalStateException("User is not authenticated");
        }
        return orderService.getMyOrders(principal.getId());
    }

    @GetMapping("/qr-preview")
    @ResponseStatus(HttpStatus.OK)
    public OrderDetailResponse getOrderPreviewByQrToken(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestParam("token") String token) {
        if ( principal == null ) {
            throw new IllegalStateException("User is not authenticated");
        }
        requireVendorOrAdmin(principal);
        return orderService.getOrderDetailByToken(principal, token);
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public OrderDetailResponse getOrderDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId){
        if ( principal == null ) {
            throw new IllegalStateException("User is not authenticated");
        }
        return orderService.getOrderDetail(principal.getId(), orderId);
    }

    @PostMapping("/{orderId}/timeout")
    @ResponseStatus(HttpStatus.OK)
    public OrderDetailResponse processTimeout(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId) {
        if ( principal == null ) {
            throw new IllegalStateException("User is not authenticated");
        }
        return orderService.processTimeout(principal.getId(), orderId);
    }

    private void requireVendorOrAdmin(UserPrincipal principal) {
        String role = principal.getRole() == null ? "" : principal.getRole();
        if (!"VENDOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vendor access required");
        }
    }

}
