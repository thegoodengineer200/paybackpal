package com.paybackpal.backend.auth.dto;

import com.paybackpal.backend.user.entity.AppUser;

import java.util.UUID;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;

    private UUID userId;
    private String name;
    private String email;

    public LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UUID userId,
            String name,
            String email
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public static LoginResponse from(AppUser user, String accessToken, long expiresInSeconds) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresInSeconds,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}