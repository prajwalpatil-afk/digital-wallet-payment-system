package com.wallet.service;

import com.wallet.dto.WalletResponse;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.exception.ApiException;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Wallet not found", HttpStatus.NOT_FOUND));

        return WalletResponse.from(wallet);
    }
}
