package com.cafetron.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String overallStatus,
        String paymentStatus,
        java.math.BigDecimal totalAmount,
        String pickupSlot,
        String location,
        String pickupTimeZone,
        String qrToken,
        LocalDateTime createdAt,
        List<OrderDetailItemResponse> items
        ) {
}
