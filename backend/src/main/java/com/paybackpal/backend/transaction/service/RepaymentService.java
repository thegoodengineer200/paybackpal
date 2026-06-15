package com.paybackpal.backend.transaction.service;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.notification.dto.NotificationOutboxResponse;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.service.ManualReminderService;
import com.paybackpal.backend.transaction.dto.TransactionSplitResponse;
import com.paybackpal.backend.transaction.entity.RepaymentStatus;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.transaction.repository.CardTransactionRepository;
import com.paybackpal.backend.transaction.repository.TransactionSplitRepository;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RepaymentService {

    private final TransactionSplitRepository transactionSplitRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final CurrentUserService currentUserService;
    private final ManualReminderService manualReminderService;

    public RepaymentService(
            TransactionSplitRepository transactionSplitRepository,
            CardTransactionRepository cardTransactionRepository,
            CurrentUserService currentUserService,
            ManualReminderService manualReminderService
    ) {
        this.transactionSplitRepository = transactionSplitRepository;
        this.cardTransactionRepository = cardTransactionRepository;
        this.currentUserService = currentUserService;
        this.manualReminderService = manualReminderService;
    }

    @Transactional(readOnly = true)
    public List<TransactionSplitResponse> getSplitsForTransaction(UUID transactionId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        boolean transactionExists = cardTransactionRepository
                .findByIdAndUser_IdAndActiveTrue(transactionId, currentUser.getId())
                .isPresent();

        if (!transactionExists) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        return transactionSplitRepository
                .findSplitsForTransactionAndUser(transactionId, currentUser.getId())
                .stream()
                .map(TransactionSplitResponse::from)
                .toList();
    }

    @Transactional
    public TransactionSplitResponse reportPaid(UUID splitId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        TransactionSplit split = getSplitForCurrentUser(splitId, currentUser.getId());

        if (split.getRepaymentStatus() == RepaymentStatus.PAYMENT_REPORTED) {
            throw new BusinessRuleViolationException("Payment has already been reported");
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("Payment is already confirmed");
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Cancelled split cannot be marked as paid");
        }

        split.markPaymentReported();

        TransactionSplit savedSplit = transactionSplitRepository.save(split);

        return TransactionSplitResponse.from(savedSplit);
    }

    @Transactional
    public TransactionSplitResponse confirmPayment(UUID splitId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        TransactionSplit split = getSplitForCurrentUser(splitId, currentUser.getId());

        if (split.getRepaymentStatus() == RepaymentStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Payment must be reported before it can be confirmed"
            );
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("Payment is already confirmed");
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Cancelled split cannot be confirmed");
        }

        split.markConfirmed();

        TransactionSplit savedSplit = transactionSplitRepository.save(split);

        return TransactionSplitResponse.from(savedSplit);
    }

    @Transactional
    public TransactionSplitResponse cancelSplit(UUID splitId) {
        AppUser currentUser = currentUserService.getCurrentUser();

        TransactionSplit split = getSplitForCurrentUser(splitId, currentUser.getId());

        if (split.getRepaymentStatus() == RepaymentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("Confirmed payment cannot be cancelled");
        }

        if (split.getRepaymentStatus() == RepaymentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Split is already cancelled");
        }

        split.markCancelled();

        TransactionSplit savedSplit = transactionSplitRepository.save(split);

        return TransactionSplitResponse.from(savedSplit);
    }

    private TransactionSplit getSplitForCurrentUser(UUID splitId, UUID currentUserId) {
        return transactionSplitRepository
                .findActiveSplitForUser(splitId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction split not found"));
    }

    @Transactional
    public NotificationOutboxResponse remindBorrower(UUID splitId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        TransactionSplit split = getSplitForCurrentUser(splitId, currentUser.getId());
        validateManualReminderAllowed(split);
        NotificationOutbox notification = manualReminderService.enqueueManualReminder(split);
        return NotificationOutboxResponse.from(notification);
    }

    private void validateManualReminderAllowed(TransactionSplit split) {
        if (split.getRepaymentStatus() == RepaymentStatus.CONFIRMED) {
            throw new BusinessRuleViolationException(
                    "Confirmed payment does not need a reminder!"
            );
        }
        if (split.getRepaymentStatus() == RepaymentStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "Cancelled split cannot be reminded!");
        }
    }
}

/*
PENDING -> report-paid -> PAYMENT_REPORTED
PAYMENT_REPORTED -> confirm -> CONFIRMED
PENDING -> cancel -> CANCELLED
PAYMENT_REPORTED -> cancel -> CANCELLED
CONFIRMED -> cancel -> rejected
CANCELLED -> report-paid -> rejected
PENDING -> confirm -> rejected
 */