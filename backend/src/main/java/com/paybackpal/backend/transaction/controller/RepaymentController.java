package com.paybackpal.backend.transaction.controller;

import com.paybackpal.backend.notification.dto.NotificationOutboxResponse;
import com.paybackpal.backend.transaction.dto.TransactionSplitResponse;
import com.paybackpal.backend.transaction.service.RepaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    @GetMapping("/transactions/{transactionId}/splits")
    public List<TransactionSplitResponse> getSplitsForTransaction(
            @PathVariable UUID transactionId
    ) {
        return repaymentService.getSplitsForTransaction(transactionId);
    }

    @PostMapping("/transaction-splits/{splitId}/report-paid")
    public TransactionSplitResponse reportPaid(
            @PathVariable UUID splitId
    ) {
        return repaymentService.reportPaid(splitId);
    }

    @PostMapping("/transaction-splits/{splitId}/confirm")
    public TransactionSplitResponse confirmPayment(
            @PathVariable UUID splitId
    ) {
        return repaymentService.confirmPayment(splitId);
    }

    @PostMapping("/transaction-splits/{splitId}/cancel")
    public TransactionSplitResponse cancelSplit(
            @PathVariable UUID splitId
    ) {
        return repaymentService.cancelSplit(splitId);
    }

    @PostMapping("/transaction-splits/{splitId}/remind")
    public NotificationOutboxResponse remindBorrower(@PathVariable UUID splitId) {
        return repaymentService.remindBorrower(splitId);
    }


}