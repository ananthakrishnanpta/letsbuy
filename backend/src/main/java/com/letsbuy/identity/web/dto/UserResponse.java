package com.letsbuy.identity.web.dto;

import com.letsbuy.identity.domain.User;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        List<String> roles,
        boolean emailVerified
) {

    public static UserResponse from(User user) {
        List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).sorted().toList();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                roles,
                user.getEmailVerifiedAt() != null
        );
    }
}
