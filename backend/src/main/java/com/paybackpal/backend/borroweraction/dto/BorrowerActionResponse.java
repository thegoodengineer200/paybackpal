package com.paybackpal.backend.borroweraction.dto;

import com.paybackpal.backend.transaction.entity.TransactionSplit;

import java.util.UUID;

public class BorrowerActionResponse {
    private final UUID transactionSplitId;
    private final String repaymentStatus;
    private final String message;

    public BorrowerActionResponse(
            UUID transactionSplitId,
            String repaymentStatus,
            String message
    ) {
        this.transactionSplitId = transactionSplitId;
        this.repaymentStatus = repaymentStatus;
        this.message = message;
    }

    public static BorrowerActionResponse paymentReported(TransactionSplit split) {
        return new BorrowerActionResponse(
                split.getId(),
                split.getRepaymentStatus().name(),
                "Payment has been reported. Waiting for owner confirmation."
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
}
