package com.cafetron.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceOrderRequest(
        // TODO: check if userId is string or Long (temp until auth)
        @NotBlank String pickupSlot,
        @NotBlank String pickupSlotTime,
        @NotNull Long pickupSlotEpochMillis,
        @NotBlank String location,
        @NotBlank String pickupTimeZone,
        @Valid @NotEmpty List<PlaceOrderItemRequest> items
) {
}
