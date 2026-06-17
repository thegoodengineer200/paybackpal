package com.paybackpal.backend.borroweraction.service;

import com.paybackpal.backend.borroweraction.config.PublicActionLinkProperties;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionResponse;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionToken;
import com.paybackpal.backend.borroweraction.entity.BorrowerActionType;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.notification.service.PaymentReportedNotificationService;
import com.paybackpal.backend.notification.service.RemindMeLaterNotificationService;
import com.paybackpal.backend.transaction.entity.RepaymentStatus;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.TransactionSplitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class PublicBorrowerActionService {

    private final BorrowerActionTokenService borrowerActionTokenService;
    private final TransactionSplitRepository transactionSplitRepository;
    private final PaymentReportedNotificationService paymentReportedNotificationService;
    private final RemindMeLaterNotificationService remindMeLaterNotificationService;
    private final PublicActionLinkProperties publicActionLinkProperties;

    public PublicBorrowerActionService(
        BorrowerActionTokenService borrowerActionTokenService,
        TransactionSplitRepository transactionSplitRepository,
        PaymentReportedNotificationService paymentReportedNotificationService,
        RemindMeLaterNotificationService remindMeLaterNotificationService,
        PublicActionLinkProperties publicActionLinkProperties
    ) {
        this.borrowerActionTokenService = borrowerActionTokenService;
        this.transactionSplitRepository = transactionSplitRepository;
        this.paymentReportedNotificationService = paymentReportedNotificationService;
        this.remindMeLaterNotificationService = remindMeLaterNotificationService;
        this.publicActionLinkProperties = publicActionLinkProperties;
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

    @Transactional
    public BorrowerActionResponse remindMeLater(String rawToken) {
        BorrowerActionToken actionToken = borrowerActionTokenService.getValidToken(rawToken, BorrowerActionType.REMIND_ME_LATER);
        TransactionSplit split = actionToken.getTransactionSplit();
        validateCanRemindMeLater(split);

        OffsetDateTime nextReminderAt = OffsetDateTime.now(ZoneOffset.UTC).plus(publicActionLinkProperties.getRemindMeLaterDelay());

        remindMeLaterNotificationService.enqueueFutureReminder(split, nextReminderAt);
        remindMeLaterNotificationService.enqueueReminderScheduledConfirmation(split, nextReminderAt);
        borrowerActionTokenService.markTokenUsed(actionToken);
        return BorrowerActionResponse.remindMeLaterScheduled(split, nextReminderAt);
    }

    private void validateCanRemindMeLater(TransactionSplit split) {
        if (split.getRepaymentStatus() == RepaymentStatus.PAYMENT_REPORTED) {
            throw new BusinessRuleViolationException(
                    "Payment has already been reported"
            );
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException(
                    "Payment is already confirmed"
            );
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "Cancelled split cannot be reminded later"
            );
        }
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
