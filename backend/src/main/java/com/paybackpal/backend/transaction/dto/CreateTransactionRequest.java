package com.paybackpal.backend.transaction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateTransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @Size(max = 150, message = "Merchant name must be at most 150 characters")
    private String merchantName;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private Boolean borrowed;

    @Valid
    private List<TransactionSplitRequest> splits;

    public CreateTransactionRequest() {
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

    public Boolean getBorrowed() {
        return borrowed;
    }

    public List<TransactionSplitRequest> getSplits() {
        return splits;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setBorrowed(Boolean borrowed) {
        this.borrowed = borrowed;
    }

    public void setSplits(List<TransactionSplitRequest> splits) {
        this.splits = splits;
    }
}