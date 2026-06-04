package com.paybackpal.backend.card.dto;

import com.paybackpal.backend.card.entity.CreditCard;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CardResponse {

    private UUID id;
    private String cardName;
    private String bankName;
    private String lastFourDigits;
    private int billingCycleDay;
    private int dueDay;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public CardResponse(
            UUID id,
            String cardName,
            String bankName,
            String lastFourDigits,
            int billingCycleDay,
            int dueDay,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.cardName = cardName;
        this.bankName = bankName;
        this.lastFourDigits = lastFourDigits;
        this.billingCycleDay = billingCycleDay;
        this.dueDay = dueDay;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CardResponse from(CreditCard card) {
        return new CardResponse(
                card.getId(),
                card.getCardName(),
                card.getBankName(),
                card.getLastFourDigits(),
                card.getBillingCycleDay(),
                card.getDueDay(),
                card.isActive(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getCardName() {
        return cardName;
    }

    public String getBankName() {
        return bankName;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public int getBillingCycleDay() {
        return billingCycleDay;
    }

    public int getDueDay() {
        return dueDay;
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