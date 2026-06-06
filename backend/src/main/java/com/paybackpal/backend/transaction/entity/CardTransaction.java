package com.paybackpal.backend.transaction.entity;

import com.paybackpal.backend.card.entity.CreditCard;
import com.paybackpal.backend.user.entity.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "card_transactions")
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCard creditCard;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(name = "merchant_name", length = 150)
    private String merchantName;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "is_borrowed", nullable = false)
    private boolean borrowed;

    @Column(name = "owner_share_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal ownerShareAmount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(
            mappedBy = "cardTransaction",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TransactionSplit> splits = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CardTransaction() {
        // Required by JPA
    }

    public CardTransaction(
            AppUser user,
            CreditCard creditCard,
            BigDecimal amount,
            String description,
            String merchantName,
            LocalDate transactionDate,
            boolean borrowed,
            BigDecimal ownerShareAmount
    ) {
        this.user = user;
        this.creditCard = creditCard;
        this.amount = amount;
        this.description = description;
        this.merchantName = merchantName;
        this.transactionDate = transactionDate;
        this.borrowed = borrowed;
        this.ownerShareAmount = ownerShareAmount;
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

    public void addSplit(TransactionSplit split) {
        splits.add(split);
        split.setCardTransaction(this);
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateOwnerShareAmount(BigDecimal ownerShareAmount) {
        this.ownerShareAmount = ownerShareAmount;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    public BigDecimal getOwnerShareAmount() {
        return ownerShareAmount;
    }

    public boolean isActive() {
        return active;
    }

    public List<TransactionSplit> getSplits() {
        return splits;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}