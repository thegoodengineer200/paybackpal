package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.borroweraction.dto.BorrowerActionLinks;
import com.paybackpal.backend.borroweraction.service.BorrowerActionLinkBuilder;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class RemindMeLaterNotificationService {

    private final NotificationOutboxService notificationOutboxService;
    private final WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;
    private final BorrowerActionLinkBuilder borrowerActionLinkBuilder;

    public RemindMeLaterNotificationService(
            NotificationOutboxService notificationOutboxService, WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder, BorrowerActionLinkBuilder borrowerActionLinkBuilder
    ) {
        this.notificationOutboxService = notificationOutboxService;
        this.whatsAppMessageTemplateBuilder = whatsAppMessageTemplateBuilder;
        this.borrowerActionLinkBuilder = borrowerActionLinkBuilder;
    }

    public NotificationOutbox enqueueFutureReminder(TransactionSplit split, OffsetDateTime nextReminderAt) {
        BorrowerActionLinks actionLinks = borrowerActionLinkBuilder.buildLinks(split);
        return notificationOutboxService.enqueueWhatsApp(
                split,
                NotificationType.MANUAL_REMINDER,
                split.getBorrower().getPhoneNumber(),
                whatsAppMessageTemplateBuilder.buildManualReminder(split, actionLinks),
                nextReminderAt
        );
    }

    public NotificationOutbox enqueueReminderScheduledConfirmation(
            TransactionSplit split, OffsetDateTime nextReminderAt
    ) {
        return notificationOutboxService.enqueueWhatsApp(
                split,
                NotificationType.REMIND_ME_LATER_CONFIGURATION,
                split.getBorrower().getPhoneNumber(), whatsAppMessageTemplateBuilder.buildRemindMeLaterConfirmation(
                        split, nextReminderAt
                ),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
