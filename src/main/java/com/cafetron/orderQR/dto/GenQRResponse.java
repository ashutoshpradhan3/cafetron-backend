package com.cafetron.orderQR.dto;

public record GenQRResponse (
        String base64QRString,
        String message
) {}
