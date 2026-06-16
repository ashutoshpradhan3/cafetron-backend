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

}
