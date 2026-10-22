package com.letsbuy.identity.web.dto;

public record AuthResponse(String accessToken, long expiresInSeconds, UserResponse user) {
}
