package com.wallet.service;

import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.Wallet;
import com.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction recordTransaction(
            Wallet wallet,
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status,
            Wallet relatedWallet,
            String referenceId,
            String description
    ) {
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .status(status)
                .relatedWallet(relatedWallet)
                .referenceId(referenceId)
                .description(description)
                .build();

        return transactionRepository.save(transaction);
    }
}
