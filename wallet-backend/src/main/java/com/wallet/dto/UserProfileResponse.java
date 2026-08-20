package com.wallet.dto;

import com.wallet.entity.Role;
import com.wallet.entity.User;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        Role role,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
