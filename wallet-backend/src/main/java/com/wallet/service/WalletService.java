package com.wallet.service;

import com.razorpay.Order;
import com.wallet.dto.CreateOrderResponse;
import com.wallet.dto.DashboardResponse;
import com.wallet.dto.TransactionResponse;
import com.wallet.dto.VerifyPaymentRequest;
import com.wallet.dto.WalletResponse;
import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.exception.ApiException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final int DASHBOARD_RECENT_LIMIT = 10;
    private static final int TRANSACTIONS_RECENT_LIMIT = 20;

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final RazorpayService razorpayService;

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

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String email) {
        Wallet wallet = requireWalletByUserEmail(email);

        BigDecimal totalDeposited = transactionRepository.sumAmountByWalletIdAndTypeAndStatus(
                wallet.getId(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS
        );
        BigDecimal totalWithdrawn = transactionRepository.sumAmountByWalletIdAndTypeAndStatus(
                wallet.getId(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS
        );

        List<TransactionResponse> recent = findRecentTransactions(wallet.getId(), DASHBOARD_RECENT_LIMIT);

        return new DashboardResponse(
                wallet.getBalance(),
                totalDeposited,
                totalWithdrawn,
                recent
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(String email) {
        Wallet wallet = requireWalletByUserEmail(email);
        return findRecentTransactions(wallet.getId(), TRANSACTIONS_RECENT_LIMIT);
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

    /**
     * Creates a Razorpay order only — no transaction row yet (outcome unknown).
     */
    @Transactional(readOnly = true)
    public CreateOrderResponse createAddMoneyOrder(String email, BigDecimal amount) {
        validatePositiveAmount(amount);
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);

        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Wallet not found", HttpStatus.NOT_FOUND));

        String receipt = "wallet_" + wallet.getId() + "_" + System.currentTimeMillis();
        Order order = razorpayService.createOrder(scaled, receipt, user.getId());
        String orderId = order.get("id");

        return new CreateOrderResponse(
                orderId,
                razorpayService.getKeyId(),
                scaled,
                "INR"
        );
    }

    /**
     * Verifies Razorpay signature, then credits wallet. First (and only) place a DEPOSIT row is written.
     */
    @Transactional
    public WalletResponse verifyAddMoney(String email, VerifyPaymentRequest request) {
        Wallet wallet = requireWalletByUserEmail(email);

        if (transactionRepository.existsByReferenceId(request.razorpayPaymentId())) {
            throw new ApiException("Payment already processed", HttpStatus.CONFLICT);
        }

        Order order = razorpayService.fetchOrder(request.razorpayOrderId());
        BigDecimal amount = razorpayService.amountRupeesFromOrder(order);

        boolean valid = razorpayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!valid) {
            transactionService.recordFailedDeposit(
                    wallet.getId(),
                    amount,
                    request.razorpayPaymentId(),
                    "Invalid Razorpay signature for order " + request.razorpayOrderId()
            );
            throw new ApiException("Invalid payment signature", HttpStatus.BAD_REQUEST);
        }

        Wallet updated = adjustBalance(wallet.getId(), amount);
        transactionService.recordTransaction(
                updated,
                TransactionType.DEPOSIT,
                amount,
                TransactionStatus.SUCCESS,
                null,
                request.razorpayPaymentId(),
                "Razorpay deposit " + request.razorpayOrderId()
        );

        return WalletResponse.from(updated);
    }

    private List<TransactionResponse> findRecentTransactions(Long walletId, int limit) {
        List<Transaction> transactions = transactionRepository.findRecentByWalletId(
                walletId,
                PageRequest.of(0, limit)
        );
        return transactions.stream().map(TransactionResponse::from).toList();
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
