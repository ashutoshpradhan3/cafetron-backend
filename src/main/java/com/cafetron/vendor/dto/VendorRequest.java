package com.cafetron.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//What the client SENDS to create or edit a vendor.
public record VendorRequest(
        @NotBlank String name,
        @Email String email,
        String phone,
        String contactPerson
){}
