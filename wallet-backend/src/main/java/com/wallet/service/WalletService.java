package com.wallet.service;

import com.wallet.dto.WalletResponse;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.exception.ApiException;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @Transactional
    public Wallet createWalletForUser(User user) {
        if (walletRepository.existsByUserId(user.getId())) {
            throw new ApiException("Wallet already exists for user", HttpStatus.CONFLICT);
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .build();

        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletForAuthenticatedUser(String email) {
        return WalletResponse.from(requireWalletByUserEmail(email));
    }

    @Transactional
    public Wallet adjustBalance(Long walletId, BigDecimal delta) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ApiException("Wallet not found", HttpStatus.NOT_FOUND));

        BigDecimal newBalance = wallet.getBalance().add(delta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }

    @Transactional
    public WalletResponse withdraw(String email, BigDecimal amount) {
        validatePositiveAmount(amount);

        Wallet wallet = requireWalletByUserEmail(email);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        Wallet updated = adjustBalance(wallet.getId(), amount.negate());
        transactionService.recordTransaction(
                updated,
                TransactionType.WITHDRAWAL,
                amount,
                TransactionStatus.SUCCESS,
                null,
                null,
                "Withdrawal"
        );

        return WalletResponse.from(updated);
    }

    @Transactional
    public WalletResponse transfer(String senderEmail, String recipientEmail, BigDecimal amount) {
        validatePositiveAmount(amount);

        String normalizedSender = normalizeEmail(senderEmail);
        String normalizedRecipient = normalizeEmail(recipientEmail);

        if (normalizedSender.equals(normalizedRecipient)) {
            throw new ApiException("Cannot transfer to yourself", HttpStatus.BAD_REQUEST);
        }

        Wallet senderWallet = requireWalletByUserEmail(normalizedSender);

        User recipientUser = userRepository.findByEmail(normalizedRecipient)
                .orElseThrow(() -> new ApiException("Recipient not found", HttpStatus.NOT_FOUND));

        Wallet recipientWallet = walletRepository.findByUserId(recipientUser.getId())
                .orElseThrow(() -> new ApiException("Recipient wallet not found", HttpStatus.NOT_FOUND));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        Wallet debited = adjustBalance(senderWallet.getId(), amount.negate());
        Wallet credited = adjustBalance(recipientWallet.getId(), amount);

        transactionService.recordTransaction(
                debited,
                TransactionType.TRANSFER_OUT,
                amount,
                TransactionStatus.SUCCESS,
                credited,
                null,
                "Transfer to " + recipientUser.getEmail()
        );
        transactionService.recordTransaction(
                credited,
                TransactionType.TRANSFER_IN,
                amount,
                TransactionStatus.SUCCESS,
                debited,
                null,
                "Transfer from " + normalizedSender
        );

        return WalletResponse.from(debited);
    }

    private Wallet requireWalletByUserEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Wallet not found", HttpStatus.NOT_FOUND));
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
