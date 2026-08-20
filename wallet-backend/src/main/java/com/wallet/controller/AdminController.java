package com.wallet.controller;

import com.wallet.dto.AdminTransactionResponse;
import com.wallet.dto.UserProfileResponse;
import com.wallet.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserProfileResponse> listUsers() {
        return adminService.listUsers();
    }

    @GetMapping("/transactions")
    public List<AdminTransactionResponse> listTransactions() {
        return adminService.listTransactions();
    }
}
