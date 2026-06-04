package com.paybackpal.backend.card.entity;

import com.paybackpal.backend.user.entity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "card_name", nullable = false, length = 100)
    private String cardName;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Column(name = "billing_cycle_day", nullable = false)
    private int billingCycleDay;

    @Column(name = "due_day", nullable = false)
    private int dueDay;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CreditCard() {}

    public CreditCard(
            AppUser user,
            String cardName,
            String bankName,
            String lastFourDigits,
            int billingCycleDay,
            int dueDay
    ) {
        this.user = user;
        this.cardName = cardName;
        this.bankName = bankName;
        this.lastFourDigits = lastFourDigits;
        this.billingCycleDay = billingCycleDay;
        this.dueDay = dueDay;
        this.active = true;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void updateDetails(
            String cardName,
            String bankName,
            String lastFourDigits,
            int billingCycleDay,
            int dueDay
    ) {
        this.cardName = cardName;
        this.bankName = bankName;
        this.lastFourDigits = lastFourDigits;
        this.billingCycleDay = billingCycleDay;
        this.dueDay = dueDay;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
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