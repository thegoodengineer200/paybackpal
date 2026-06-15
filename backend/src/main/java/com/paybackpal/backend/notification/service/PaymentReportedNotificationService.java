package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class PaymentReportedNotificationService {
    private final NotificationOutboxService notificationOutboxService;
    private final WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;

    public PaymentReportedNotificationService(
            NotificationOutboxService notificationOutboxService, WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder
    ) {
        this.notificationOutboxService = notificationOutboxService;
        this.whatsAppMessageTemplateBuilder = whatsAppMessageTemplateBuilder;
    }

    public NotificationOutbox enqueuePaymentReportedToOwner(TransactionSplit split) {
        return notificationOutboxService.enqueueWhatsApp(
                split,
                NotificationType.PAYMENT_REPORTED_TO_OWNER,
                split.getCardTransaction().getUser().getPhoneNumber(),
                whatsAppMessageTemplateBuilder.buildPaymentReportedToOwner(split),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}
