package com.paybackpal.backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BorrowerSummaryResponse {

    private final UUID borrowerId;
    private final String borrowerName;
    private final String borrowerPhoneNumber;

    private final BigDecimal totalBorrowerShareAmount;
    private final BigDecimal pendingAmount;
    private final BigDecimal paymentReportedAmount;
    private final BigDecimal confirmedAmount;
    private final BigDecimal cancelledAmount;

    private final long splitCount;

    public BorrowerSummaryResponse(
            UUID borrowerId,
            String borrowerName,
            String borrowerPhoneNumber,
            BigDecimal totalBorrowerShareAmount,
            BigDecimal pendingAmount,
            BigDecimal paymentReportedAmount,
            BigDecimal confirmedAmount,
            BigDecimal cancelledAmount,
            long splitCount
    ) {
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.borrowerPhoneNumber = borrowerPhoneNumber;
        this.totalBorrowerShareAmount = totalBorrowerShareAmount;
        this.pendingAmount = pendingAmount;
        this.paymentReportedAmount = paymentReportedAmount;
        this.confirmedAmount = confirmedAmount;
        this.cancelledAmount = cancelledAmount;
        this.splitCount = splitCount;
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

    public BigDecimal getTotalBorrowerShareAmount() {
        return totalBorrowerShareAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public BigDecimal getPaymentReportedAmount() {
        return paymentReportedAmount;
    }

    public BigDecimal getConfirmedAmount() {
        return confirmedAmount;
    }

    public BigDecimal getCancelledAmount() {
        return cancelledAmount;
    }

    public long getSplitCount() {
        return splitCount;
    }
}