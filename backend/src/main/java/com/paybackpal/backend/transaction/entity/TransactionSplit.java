package com.paybackpal.backend.transaction.entity;

import com.paybackpal.backend.borrower.entity.Borrower;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "transaction_splits")
public class TransactionSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private CardTransaction cardTransaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @Column(name = "split_percentage", precision = 5, scale = 2)
    private BigDecimal splitPercentage;

    @Column(name = "split_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal splitAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_status", nullable = false, length = 30)
    private RepaymentStatus repaymentStatus = RepaymentStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected TransactionSplit() {}

    public TransactionSplit(
            Borrower borrower,
            BigDecimal splitPercentage,
            BigDecimal splitAmount
    ) {
        this.borrower = borrower;
        this.splitPercentage = splitPercentage;
        this.splitAmount = splitAmount;
        this.repaymentStatus = RepaymentStatus.PENDING;
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

    void setCardTransaction(CardTransaction cardTransaction) {
        this.cardTransaction = cardTransaction;
    }

    public UUID getId() {
        return id;
    }

    public CardTransaction getCardTransaction() {
        return cardTransaction;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public BigDecimal getSplitPercentage() {
        return splitPercentage;
    }

    public BigDecimal getSplitAmount() {
        return splitAmount;
    }

    public RepaymentStatus getRepaymentStatus() {
        return repaymentStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markPaymentReported() {
        this.repaymentStatus = RepaymentStatus.PAYMENT_REPORTED;
    }

    public void markConfirmed() {
        this.repaymentStatus = RepaymentStatus.CONFIRMED;
    }

    public void markCancelled() {
        this.repaymentStatus = RepaymentStatus.CANCELLED;
    }
}