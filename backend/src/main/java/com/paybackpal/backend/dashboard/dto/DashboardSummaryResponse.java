package com.paybackpal.backend.dashboard.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private final BigDecimal totalCardSpendAmount;
    private final BigDecimal ownerExpenseAmount;

    private final BigDecimal personalTransactionAmount;
    private final BigDecimal borrowedTransactionAmount;

    private final BigDecimal totalBorrowerShareAmount;
    private final BigDecimal pendingAmount;
    private final BigDecimal paymentReportedAmount;
    private final BigDecimal confirmedAmount;
    private final BigDecimal cancelledAmount;

    private final long transactionCount;
    private final long personalTransactionCount;
    private final long borrowedTransactionCount;
    private final long splitCount;

    public DashboardSummaryResponse(
            BigDecimal totalCardSpendAmount,
            BigDecimal ownerExpenseAmount,
            BigDecimal personalTransactionAmount,
            BigDecimal borrowedTransactionAmount,
            BigDecimal totalBorrowerShareAmount,
            BigDecimal pendingAmount,
            BigDecimal paymentReportedAmount,
            BigDecimal confirmedAmount,
            BigDecimal cancelledAmount,
            long transactionCount,
            long personalTransactionCount,
            long borrowedTransactionCount,
            long splitCount
    ) {
        this.totalCardSpendAmount = totalCardSpendAmount;
        this.ownerExpenseAmount = ownerExpenseAmount;
        this.personalTransactionAmount = personalTransactionAmount;
        this.borrowedTransactionAmount = borrowedTransactionAmount;
        this.totalBorrowerShareAmount = totalBorrowerShareAmount;
        this.pendingAmount = pendingAmount;
        this.paymentReportedAmount = paymentReportedAmount;
        this.confirmedAmount = confirmedAmount;
        this.cancelledAmount = cancelledAmount;
        this.transactionCount = transactionCount;
        this.personalTransactionCount = personalTransactionCount;
        this.borrowedTransactionCount = borrowedTransactionCount;
        this.splitCount = splitCount;
    }

    public BigDecimal getTotalCardSpendAmount() {
        return totalCardSpendAmount;
    }

    public BigDecimal getOwnerExpenseAmount() {
        return ownerExpenseAmount;
    }

    public BigDecimal getPersonalTransactionAmount() {
        return personalTransactionAmount;
    }

    public BigDecimal getBorrowedTransactionAmount() {
        return borrowedTransactionAmount;
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

    public long getTransactionCount() {
        return transactionCount;
    }

    public long getPersonalTransactionCount() {
        return personalTransactionCount;
    }

    public long getBorrowedTransactionCount() {
        return borrowedTransactionCount;
    }

    public long getSplitCount() {
        return splitCount;
    }
}