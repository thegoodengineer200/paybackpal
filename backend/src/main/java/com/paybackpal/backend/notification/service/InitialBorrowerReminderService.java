package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class InitialBorrowerReminderService {

    private final NotificationOutboxService notificationOutboxService;

    public InitialBorrowerReminderService(NotificationOutboxService notificationOutboxService) {
        this.notificationOutboxService = notificationOutboxService;
    }

    public void enqueueInitialReminders(CardTransaction transaction) {
        if (!transaction.isBorrowed()) {
            return;
        }

        OffsetDateTime scheduledAt = OffsetDateTime.now(ZoneOffset.UTC);

        for (TransactionSplit split: transaction.getSplits()) {
            Borrower borrower = split.getBorrower();
            notificationOutboxService.enqueueWhatsApp(
                    split,
                    NotificationType.INITIAL_PAYMENT_REQUEST,
                    borrower.getPhoneNumber(),
                    buildMessageBody(transaction, split),
                    scheduledAt
            );
        }
    }

    private String buildMessageBody(CardTransaction transaction, TransactionSplit split) {
        AppUser owner = transaction.getUser();
        Borrower borrower = split.getBorrower();
        String transactionLabel = getTransactionLabel(transaction);
        String amount = formatMoney(split.getSplitAmount());

        StringBuilder message = new StringBuilder();

        message.append("Hi ")
                .append(borrower.getName())
                .append(", ")
                .append(owner.getName())
                .append(" added ₹")
                .append(amount)
                .append(" as your share for ")
                .append(transactionLabel)
                .append(".");

        if (owner.getUpiId() != null && !owner.getUpiId().isBlank()) {
            message.append("\nUPI: ").append(owner.getUpiId());
        }

        message.append("\nPlease pay your share.")
                .append("\nActions: Paid | Remind me later");
        return message.toString();
    }

    private String getTransactionLabel(CardTransaction transaction) {
        if (transaction.getMerchantName() != null && !transaction.getMerchantName().isBlank()) {
            return transaction.getMerchantName();
        }
        if (transaction.getDescription() != null && !transaction.getDescription().isBlank()) {
            return transaction.getDescription();
        }
        return "this transaction";
    }

    private String formatMoney(BigDecimal amount)  {
        return amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }


}
