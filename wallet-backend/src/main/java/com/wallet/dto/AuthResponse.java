package com.wallet.dto;

import com.wallet.entity.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        String name,
        String email,
        Role role
) {
    public static AuthResponse of(String accessToken, long expiresInSeconds, Long userId, String name, String email, Role role) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, userId, name, email, role);
    }
}
