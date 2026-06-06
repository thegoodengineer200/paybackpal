package com.paybackpal.backend.transaction.dto;

import com.paybackpal.backend.transaction.entity.CardTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionResponse {

    private final UUID id;
    private final UUID cardId;
    private final String cardName;
    private final String bankName;
    private final String lastFourDigits;

    private final BigDecimal amount;
    private final String description;
    private final String merchantName;
    private final LocalDate transactionDate;

    private final boolean borrowed;
    private final BigDecimal ownerShareAmount;
    private final List<TransactionSplitResponse> splits;

    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public TransactionResponse(
            UUID id,
            UUID cardId,
            String cardName,
            String bankName,
            String lastFourDigits,
            BigDecimal amount,
            String description,
            String merchantName,
            LocalDate transactionDate,
            boolean borrowed,
            BigDecimal ownerShareAmount,
            List<TransactionSplitResponse> splits,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.cardId = cardId;
        this.cardName = cardName;
        this.bankName = bankName;
        this.lastFourDigits = lastFourDigits;
        this.amount = amount;
        this.description = description;
        this.merchantName = merchantName;
        this.transactionDate = transactionDate;
        this.borrowed = borrowed;
        this.ownerShareAmount = ownerShareAmount;
        this.splits = splits;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TransactionResponse from(CardTransaction transaction) {
        List<TransactionSplitResponse> splitResponses = transaction.getSplits()
                .stream()
                .map(TransactionSplitResponse::from)
                .toList();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getCreditCard().getId(),
                transaction.getCreditCard().getCardName(),
                transaction.getCreditCard().getBankName(),
                transaction.getCreditCard().getLastFourDigits(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getMerchantName(),
                transaction.getTransactionDate(),
                transaction.isBorrowed(),
                transaction.getOwnerShareAmount(),
                splitResponses,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getCardId() {
        return cardId;
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

    public List<TransactionSplitResponse> getSplits() {
        return splits;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}