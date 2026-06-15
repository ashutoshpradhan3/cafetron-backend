package com.cafetron.order.controller;

import com.cafetron.order.dto.PlaceOrderRequest;
import com.cafetron.order.dto.PlaceOrderResponse;
import com.cafetron.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // When Jwt is configured, this will change
    public PlaceOrderResponse placeOrder(@RequestParam Long userId, @Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(userId,request);
    }
}
