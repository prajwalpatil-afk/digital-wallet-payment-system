package com.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least ₹1.00")
        BigDecimal amount
) {
}
