package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borroweraction.dto.BorrowerActionLinks;
import com.paybackpal.backend.borroweraction.service.BorrowerActionLinkBuilder;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
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
    private final WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;
    private final BorrowerActionLinkBuilder borrowerActionLinkBuilder;

    public InitialBorrowerReminderService(NotificationOutboxService notificationOutboxService,
                                          WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder,
                                          BorrowerActionLinkBuilder borrowerActionLinkBuilder) {
        this.notificationOutboxService = notificationOutboxService;
        this.whatsAppMessageTemplateBuilder = whatsAppMessageTemplateBuilder;
        this.borrowerActionLinkBuilder = borrowerActionLinkBuilder;
    }

    public void enqueueInitialReminders(CardTransaction transaction) {
        if (!transaction.isBorrowed()) {
            return;
        }

        OffsetDateTime scheduledAt = OffsetDateTime.now(ZoneOffset.UTC);

        for (TransactionSplit split: transaction.getSplits()) {
            Borrower borrower = split.getBorrower();
            BorrowerActionLinks actionLinks = borrowerActionLinkBuilder.buildLinks(split);
            notificationOutboxService.enqueueWhatsApp(
                    split,
                    NotificationType.INITIAL_PAYMENT_REQUEST,
                    borrower.getPhoneNumber(),
                    whatsAppMessageTemplateBuilder.buildInitialPaymentRequest(split, actionLinks),
                    scheduledAt
            );
        }
    }

}
