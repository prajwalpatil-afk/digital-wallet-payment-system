package com.wallet.service;

import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.Wallet;
import com.wallet.exception.ApiException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

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

    /**
     * Persists immediately in its own transaction so a subsequent exception
     * in the caller (e.g. invalid payment signature) does not roll it back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction recordFailedDeposit(Long walletId, BigDecimal amount, String referenceId, String description) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ApiException("Wallet not found", HttpStatus.NOT_FOUND));

        return recordTransaction(
                wallet,
                TransactionType.DEPOSIT,
                amount,
                TransactionStatus.FAILED,
                null,
                referenceId,
                description
        );
    }
}
