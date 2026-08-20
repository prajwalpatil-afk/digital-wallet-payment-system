package com.wallet.dto;

import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminTransactionResponse(
        Long id,
        Long walletId,
        Long userId,
        String userEmail,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        Long relatedWalletId,
        String referenceId,
        String description,
        Instant createdAt
) {
    public static AdminTransactionResponse from(Transaction transaction) {
        Wallet wallet = transaction.getWallet();
        User user = wallet.getUser();
        Long relatedWalletId = transaction.getRelatedWallet() != null
                ? transaction.getRelatedWallet().getId()
                : null;

        return new AdminTransactionResponse(
                transaction.getId(),
                wallet.getId(),
                user.getId(),
                user.getEmail(),
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
