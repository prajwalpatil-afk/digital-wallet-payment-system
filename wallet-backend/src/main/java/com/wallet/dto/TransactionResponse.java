package com.wallet.dto;

import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        Long relatedWalletId,
        String referenceId,
        String description,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        Long relatedWalletId = transaction.getRelatedWallet() != null
                ? transaction.getRelatedWallet().getId()
                : null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                relatedWalletId,
                transaction.getReferenceId(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
