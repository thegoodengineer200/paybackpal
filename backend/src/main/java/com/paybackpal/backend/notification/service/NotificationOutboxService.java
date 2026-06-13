package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.transaction.entity.TransactionSplit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class NotificationOutboxService {

    private final NotificationOutboxRepository notificationOutboxRepository;

    public NotificationOutboxService(NotificationOutboxRepository notificationOutboxRepository) {
        this.notificationOutboxRepository = notificationOutboxRepository;
    }

    @Transactional
    public NotificationOutbox enqueueWhatsApp(TransactionSplit transactionSplit, NotificationType notificationType, String recipientPhoneNumber, String messageBody, OffsetDateTime scheduledAt) {
        NotificationOutbox notification = new NotificationOutbox(
                transactionSplit,
                NotificationChannel.WHATSAPP,
                notificationType,
                recipientPhoneNumber,
                messageBody,
                scheduledAt
        );

        return notificationOutboxRepository.save(notification);
    }
}