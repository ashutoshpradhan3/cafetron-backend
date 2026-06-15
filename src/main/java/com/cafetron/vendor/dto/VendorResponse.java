package com.cafetron.vendor.dto;

import java.time.LocalDateTime;

// What the API SENDS BACK for a vendor.
public record VendorResponse(
        Long id,
        String name,
        String email,
        String phone,
        String contactPerson,
        boolean isActive,
        LocalDateTime createdAt
) {}
