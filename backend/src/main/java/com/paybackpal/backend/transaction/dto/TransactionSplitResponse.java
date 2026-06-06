package com.paybackpal.backend.transaction.dto;

import com.paybackpal.backend.transaction.entity.TransactionSplit;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionSplitResponse {

    private final UUID id;
    private final UUID borrowerId;
    private final String borrowerName;
    private final String borrowerPhoneNumber;
    private final BigDecimal splitPercentage;
    private final BigDecimal splitAmount;
    private final String repaymentStatus;

    public TransactionSplitResponse(
            UUID id,
            UUID borrowerId,
            String borrowerName,
            String borrowerPhoneNumber,
            BigDecimal splitPercentage,
            BigDecimal splitAmount,
            String repaymentStatus
    ) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.borrowerPhoneNumber = borrowerPhoneNumber;
        this.splitPercentage = splitPercentage;
        this.splitAmount = splitAmount;
        this.repaymentStatus = repaymentStatus;
    }

    public static TransactionSplitResponse from(TransactionSplit split) {
        return new TransactionSplitResponse(
                split.getId(),
                split.getBorrower().getId(),
                split.getBorrower().getName(),
                split.getBorrower().getPhoneNumber(),
                split.getSplitPercentage(),
                split.getSplitAmount(),
                split.getRepaymentStatus().name()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getBorrowerId() {
        return borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getBorrowerPhoneNumber() {
        return borrowerPhoneNumber;
    }

    public BigDecimal getSplitPercentage() {
        return splitPercentage;
    }

    public BigDecimal getSplitAmount() {
        return splitAmount;
    }

    public String getRepaymentStatus() {
        return repaymentStatus;
    }
}