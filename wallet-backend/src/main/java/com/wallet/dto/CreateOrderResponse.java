package com.wallet.dto;

import java.math.BigDecimal;

public record CreateOrderResponse(
        String orderId,
        String keyId,
        BigDecimal amount,
        String currency
) {
}
