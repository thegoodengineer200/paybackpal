package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class ManualReminderService {

    private final NotificationOutboxService notificationOutboxService;
    private final WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;

    public ManualReminderService(
            NotificationOutboxService notificationOutboxService,
            WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder
    ) {
        this.notificationOutboxService = notificationOutboxService;
        this.whatsAppMessageTemplateBuilder = whatsAppMessageTemplateBuilder;
    }

    public NotificationOutbox enqueueManualReminder(TransactionSplit split) {
        return notificationOutboxService.enqueueWhatsApp(
                split,
                NotificationType.MANUAL_REMINDER,
                split.getBorrower().getPhoneNumber(),
                whatsAppMessageTemplateBuilder.buildManualReminder(split),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}