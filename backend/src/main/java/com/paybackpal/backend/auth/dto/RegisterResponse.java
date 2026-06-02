package com.paybackpal.backend.auth.dto;

import com.paybackpal.backend.user.entity.AppUser;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RegisterResponse {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String upiId;
    private OffsetDateTime createdAt;

    public RegisterResponse(
            UUID id,
            String name,
            String email,
            String phoneNumber,
            String upiId,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.upiId = upiId;
        this.createdAt = createdAt;
    }

    public static RegisterResponse from(AppUser user) {
        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUpiId(),
                user.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUpiId() {
        return upiId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}