package com.paybackpal.backend.borroweraction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paybackpal.backend.transaction.entity.TransactionSplit;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BorrowerActionResponse {

    private final UUID transactionSplitId;
    private final String repaymentStatus;
    private final String message;
    private final OffsetDateTime nextReminderAt;

    public BorrowerActionResponse(
            UUID transactionSplitId,
            String repaymentStatus,
            String message,
            OffsetDateTime nextReminderAt
    ) {
        this.transactionSplitId = transactionSplitId;
        this.repaymentStatus = repaymentStatus;
        this.message = message;
        this.nextReminderAt = nextReminderAt;
    }

    public static BorrowerActionResponse paymentReported(TransactionSplit split) {
        return new BorrowerActionResponse(
                split.getId(),
                split.getRepaymentStatus().name(),
                "Payment has been reported. Waiting for owner confirmation.",
                null
        );
    }

    public static BorrowerActionResponse remindMeLaterScheduled(
            TransactionSplit split,
            OffsetDateTime nextReminderAt
    ) {
        return new BorrowerActionResponse(
                split.getId(),
                split.getRepaymentStatus().name(),
                "No problem. We will remind you later.",
                nextReminderAt
        );
    }

    public UUID getTransactionSplitId() {
        return transactionSplitId;
    }

    public String getRepaymentStatus() {
        return repaymentStatus;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getNextReminderAt() {
        return nextReminderAt;
    }
}