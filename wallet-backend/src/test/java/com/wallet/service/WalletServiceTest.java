package com.wallet.service;

import com.razorpay.Order;
import com.wallet.dto.VerifyPaymentRequest;
import com.wallet.dto.WalletResponse;
import com.wallet.entity.Role;
import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.exception.ApiException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RazorpayService razorpayService;

    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        sender = createUserWithWallet("sender@example.com", "Sender");
        recipient = createUserWithWallet("recipient@example.com", "Recipient");
        credit(sender, new BigDecimal("500.00"));
    }

    @Test
    void deposit_increasesBalance_andRecordsTransaction() {
        Order order = mock(Order.class);
        when(razorpayService.fetchOrder("order_abc")).thenReturn(order);
        when(razorpayService.amountRupeesFromOrder(order)).thenReturn(new BigDecimal("100.00"));
        when(razorpayService.verifySignature("order_abc", "pay_abc", "valid_sig")).thenReturn(true);

        BigDecimal before = walletRepository.findByUserId(sender.getId()).orElseThrow().getBalance();

        WalletResponse response = walletService.verifyAddMoney(
                sender.getEmail(),
                new VerifyPaymentRequest("order_abc", "pay_abc", "valid_sig")
        );

        assertThat(response.balance()).isEqualByComparingTo(before.add(new BigDecimal("100.00")));

        List<Transaction> deposits = transactionRepository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT)
                .toList();
        assertThat(deposits).hasSize(1);
        assertThat(deposits.getFirst().getAmount()).isEqualByComparingTo("100.00");
        assertThat(deposits.getFirst().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(deposits.getFirst().getReferenceId()).isEqualTo("pay_abc");
    }

    @Test
    void withdraw_success() {
        WalletResponse response = walletService.withdraw(sender.getEmail(), new BigDecimal("150.00"));

        assertThat(response.balance()).isEqualByComparingTo("350.00");

        List<Transaction> withdrawals = transactionRepository.findAll().stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
                .toList();
        assertThat(withdrawals).hasSize(1);
        assertThat(withdrawals.getFirst().getAmount()).isEqualByComparingTo("150.00");
        assertThat(withdrawals.getFirst().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    void withdraw_insufficientBalance_throws() {
        assertThatThrownBy(() -> walletService.withdraw(sender.getEmail(), new BigDecimal("999.00")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(apiEx.getMessage()).isEqualTo("Insufficient balance");
                });

        assertThat(walletRepository.findByUserId(sender.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("500.00");
    }

    @Test
    void transfer_success_bothWalletsUpdated() {
        WalletResponse response = walletService.transfer(
                sender.getEmail(),
                recipient.getEmail(),
                new BigDecimal("200.00")
        );

        assertThat(response.balance()).isEqualByComparingTo("300.00");
        assertThat(walletRepository.findByUserId(recipient.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("200.00");

        List<Transaction> all = transactionRepository.findAll();
        assertThat(all).anyMatch(t ->
                t.getType() == TransactionType.TRANSFER_OUT
                        && t.getAmount().compareTo(new BigDecimal("200.00")) == 0
                        && t.getStatus() == TransactionStatus.SUCCESS
        );
        assertThat(all).anyMatch(t ->
                t.getType() == TransactionType.TRANSFER_IN
                        && t.getAmount().compareTo(new BigDecimal("200.00")) == 0
                        && t.getStatus() == TransactionStatus.SUCCESS
        );
    }

    @Test
    void transfer_invalidRecipient_throws() {
        assertThatThrownBy(() -> walletService.transfer(
                sender.getEmail(),
                "nobody@example.com",
                new BigDecimal("50.00")
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiEx = (ApiException) ex;
                    assertThat(apiEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(apiEx.getMessage()).isEqualTo("Recipient not found");
                });

        assertThat(walletRepository.findByUserId(sender.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("500.00");
    }

    private User createUserWithWallet(String email, String name) {
        User user = userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode("secret123"))
                .role(Role.USER)
                .build());
        walletService.createWalletForUser(user);
        return user;
    }

    private void credit(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(user.getId()).orElseThrow();
        wallet.setBalance(amount);
        walletRepository.save(wallet);
    }
}
