package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.template.WhatsAppMessageTemplateBuilder;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class PaymentConfirmedNotificationService {

    private final NotificationOutboxService notificationOutboxService;
    private final WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder;

    public PaymentConfirmedNotificationService(
            NotificationOutboxService notificationOutboxService,
            WhatsAppMessageTemplateBuilder whatsAppMessageTemplateBuilder
    ) {
        this.notificationOutboxService = notificationOutboxService;
        this.whatsAppMessageTemplateBuilder = whatsAppMessageTemplateBuilder;
    }

    public NotificationOutbox enqueuePaymentConfirmedToBorrower(TransactionSplit split) {
        return notificationOutboxService.enqueueWhatsApp(
                split,
                NotificationType.PAYMENT_CONFIRMED_TO_BORROWER,
                split.getBorrower().getPhoneNumber(),
                whatsAppMessageTemplateBuilder.buildPaymentConfirmedToBorrower(split),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}