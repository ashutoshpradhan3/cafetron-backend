package com.cafetron.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyOrderSummaryResponse(
        Long orderId,
        String overallStatus,
        String paymentStatus,
        BigDecimal totalAmount,
        String pickupSlot,
        String location,
        String pickupTimeZone,
        LocalDateTime createdAt
        ) {}


