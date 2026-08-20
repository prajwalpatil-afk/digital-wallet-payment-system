package com.wallet.controller;

import com.wallet.dto.CreateOrderRequest;
import com.wallet.dto.CreateOrderResponse;
import com.wallet.dto.TransferRequest;
import com.wallet.dto.VerifyPaymentRequest;
import com.wallet.dto.WalletResponse;
import com.wallet.dto.WithdrawRequest;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public WalletResponse getWallet(Authentication authentication) {
        return walletService.getWalletForAuthenticatedUser(authentication.getName());
    }

    @PostMapping("/withdraw")
    public WalletResponse withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication
    ) {
        return walletService.withdraw(authentication.getName(), request.amount());
    }

    @PostMapping("/transfer")
    public WalletResponse transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication
    ) {
        return walletService.transfer(
                authentication.getName(),
                request.recipientEmail(),
                request.amount()
        );
    }

    @PostMapping("/add-money/order")
    public CreateOrderResponse createAddMoneyOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {
        return walletService.createAddMoneyOrder(authentication.getName(), request.amount());
    }

    @PostMapping("/add-money/verify")
    public WalletResponse verifyAddMoney(
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication
    ) {
        return walletService.verifyAddMoney(authentication.getName(), request);
    }
}
