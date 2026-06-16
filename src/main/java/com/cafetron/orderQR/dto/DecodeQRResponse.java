package com.cafetron.orderQR.dto;

public record DecodeQRResponse(
        boolean isValid,
        String token,
        String message
) {}

