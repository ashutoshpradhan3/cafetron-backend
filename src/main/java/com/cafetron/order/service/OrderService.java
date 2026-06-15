package com.cafetron.order.service;

import com.cafetron.order.dto.PlaceOrderRequest;
import com.cafetron.order.dto.PlaceOrderResponse;

public interface OrderService {
    PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request);
}