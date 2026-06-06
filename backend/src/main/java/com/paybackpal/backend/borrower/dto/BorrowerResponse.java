package com.paybackpal.backend.borrower.dto;

import com.paybackpal.backend.borrower.entity.Borrower;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BorrowerResponse {

    private final UUID id;
    private final String name;
    private final String phoneNumber;
    private final boolean active;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public BorrowerResponse(UUID id, String name, String phoneNumber, boolean active,OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BorrowerResponse from(Borrower borrower) {
        return new BorrowerResponse(
                borrower.getId(),
                borrower.getName(),
                borrower.getPhoneNumber(),
                borrower.isActive(),
                borrower.getCreatedAt(),
                borrower.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}