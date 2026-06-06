package com.paybackpal.backend.transaction.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionSplitRequest {

    @NotNull(message = "Borrower id is required")
    private UUID borrowerId;

    @DecimalMin(value = "0.01", message = "Split percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Split percentage must be at most 100")
    @Digits(integer = 3, fraction = 2, message = "Split percentage must have at most 2 decimal places")
    private BigDecimal splitPercentage;

    public TransactionSplitRequest() {
    }

    public UUID getBorrowerId() {
        return borrowerId;
    }

    public BigDecimal getSplitPercentage() {
        return splitPercentage;
    }

    public void setBorrowerId(UUID borrowerId) {
        this.borrowerId = borrowerId;
    }

    public void setSplitPercentage(BigDecimal splitPercentage) {
        this.splitPercentage = splitPercentage;
    }
}