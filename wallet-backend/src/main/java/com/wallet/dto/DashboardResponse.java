package com.wallet.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal balance,
        BigDecimal totalDeposited,
        BigDecimal totalWithdrawn,
        List<TransactionResponse> recentTransactions
) {
}
