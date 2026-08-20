package com.wallet.service;

import com.wallet.dto.AdminTransactionResponse;
import com.wallet.dto.UserProfileResponse;
import com.wallet.entity.Transaction;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<UserProfileResponse> listUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(UserProfileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTransactionResponse> listTransactions() {
        List<Transaction> transactions = transactionRepository.findAllForAdmin();
        return transactions.stream()
                .map(AdminTransactionResponse::from)
                .toList();
    }
}
