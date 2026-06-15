package com.paybackpal.backend.notification.template;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.transaction.entity.CardTransaction;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WhatsAppMessageTemplateBuilder {

    private static final DateTimeFormatter REMINDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public String buildInitialPaymentRequest(TransactionSplit split) {
        CardTransaction transaction = split.getCardTransaction();
        AppUser owner = transaction.getUser();
        Borrower borrower = split.getBorrower();

        StringBuilder message = new StringBuilder();

        message.append("Hi ")
                .append(borrower.getName())
                .append(", ")
                .append(owner.getName())
                .append(" added ₹")
                .append(formatMoney(split.getSplitAmount()))
                .append(" as your share for ")
                .append(getTransactionLabel(transaction))
                .append(".");

        appendUpiLineIfPresent(message, owner);

        message.append("\nPlease pay your share.").append("\nActions: Paid | Remind me later");
        return message.toString();
    }

    public String buildManualReminder(TransactionSplit split) {
        CardTransaction transaction = split.getCardTransaction();
        AppUser owner = transaction.getUser();
        Borrower borrower = split.getBorrower();
        StringBuilder message = new StringBuilder();

        message.append("Hi ")
                .append(borrower.getName())
                .append(", gentle reminder from ")
                .append(owner.getName())
                .append(" to pay ₹")
                .append(formatMoney(split.getSplitAmount()))
                .append(" for ")
                .append(getTransactionLabel(transaction))
                .append(".");

        appendUpiLineIfPresent(message, owner);
        message.append("\nActions: Paid | Remind me later");
        return message.toString();
    }

    public String buildPaymentReportedToOwner(TransactionSplit split) {
        CardTransaction transaction = split.getCardTransaction();
        AppUser owner = transaction.getUser();
        Borrower borrower = split.getBorrower();

        return new StringBuilder()
                .append("Hi ")
                .append(owner.getName())
                .append(", ")
                .append(borrower.getName())
                .append(" has reported payment of ₹")
                .append(formatMoney(split.getSplitAmount()))
                .append(" for ")
                .append(getTransactionLabel(transaction))
                .append(".")
                .append("\nPlease confirm once you have received it.")
                .toString();
    }

    public String buildPaymentConfirmedToBorrower(TransactionSplit split) {
        CardTransaction transaction = split.getCardTransaction();
        AppUser owner = transaction.getUser();
        Borrower borrower = split.getBorrower();

        return new StringBuilder()
                .append("Hi ")
                .append(borrower.getName())
                .append(", your payment of ₹")
                .append(formatMoney(split.getSplitAmount()))
                .append(" for ")
                .append(getTransactionLabel(transaction))
                .append(" has been confirmed by ")
                .append(owner.getName())
                .append(".")
                .append("\nThanks for settling up!")
                .toString();
    }

    public String buildRemindMeLaterConfirmation(
            TransactionSplit split,
            OffsetDateTime nextReminderAt
    ) {
        CardTransaction transaction = split.getCardTransaction();
        Borrower borrower = split.getBorrower();

        return new StringBuilder()
                .append("Hi ")
                .append(borrower.getName())
                .append(", no problem.")
                .append("\nWe will remind you again for ₹")
                .append(formatMoney(split.getSplitAmount()))
                .append(" related to ")
                .append(getTransactionLabel(transaction))
                .append(" on ")
                .append(formatReminderTime(nextReminderAt))
                .append(".")
                .toString();
    }

    private void appendUpiLineIfPresent(StringBuilder message, AppUser owner) {
        if (owner.getUpiId() == null || owner.getUpiId().isBlank()) {
            return;
        }

        message.append("\nUPI: ").append(owner.getUpiId());
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

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    private String formatReminderTime(OffsetDateTime nextReminderAt) {
        return nextReminderAt.format(REMINDER_TIME_FORMATTER);
    }
}