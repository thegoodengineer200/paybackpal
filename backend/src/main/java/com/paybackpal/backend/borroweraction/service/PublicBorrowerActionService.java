package com.paybackpal.backend.borroweraction.service;

import com.paybackpal.backend.borroweraction.dto.BorrowerActionResponse;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.notification.service.PaymentReportedNotificationService;
import com.paybackpal.backend.transaction.entity.RepaymentStatus;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.TransactionSplitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicBorrowerActionService {

    private final BorrowerActionTokenService borrowerActionTokenService;
    private final TransactionSplitRepository transactionSplitRepository;
    private final PaymentReportedNotificationService paymentReportedNotificationService;

    public PublicBorrowerActionService(
            BorrowerActionTokenService borrowerActionTokenService, TransactionSplitRepository transactionSplitRepository,
            PaymentReportedNotificationService paymentReportedNotificationService
    ) {
        this.borrowerActionTokenService = borrowerActionTokenService;
        this.transactionSplitRepository = transactionSplitRepository;
        this.paymentReportedNotificationService = paymentReportedNotificationService;
    }

    @Transactional
    public BorrowerActionResponse reportPaid(String rawToken) {
        BorrowerActionToken actionToken = borrowerActionTokenService.getValidToken(
                rawToken,
                BorrowerActionType.REPORT_PAID
        );

        TransactionSplit split = actionToken.getTransactionSplit();
        validateCanReportPaid(split);
        split.markPaymentReported();
        TransactionSplit savedSplit = transactionSplitRepository.save(split);
        borrowerActionTokenService.markTokenUsed(actionToken);
        paymentReportedNotificationService.enqueuePaymentReportedToOwner(savedSplit);
        return BorrowerActionResponse.paymentReported(savedSplit);
    }

    private void validateCanReportPaid(TransactionSplit split) {
        if (split.getRepaymentStatus() == RepaymentStatus.PAYMENT_REPORTED) {
            throw new BusinessRuleViolationException("Payment has already been reported!");
        }
        if (split.getRepaymentStatus() == RepaymentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("Payment is already confirmed.");
        }
        if (split.getRepaymentStatus() == RepaymentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Canceled split cannot be marked as paid");
        }
    }
}
