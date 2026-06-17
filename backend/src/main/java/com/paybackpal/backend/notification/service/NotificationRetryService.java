package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.notification.config.NotificationWorkerProperties;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class NotificationRetryService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationWorkerProperties notificationWorkerProperties;

    public NotificationRetryService(
            NotificationOutboxRepository notificationOutboxRepository,
            NotificationWorkerProperties notificationWorkerProperties
    ) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.notificationWorkerProperties = notificationWorkerProperties;
    }

    @Transactional
    public void scheduleRetryIfEligible(NotificationOutbox dispatchedNotification) {
        if (dispatchedNotification.getStatus() != NotificationStatus.FAILED) {
            return;
        }

        if (dispatchedNotification.getRetryCount()
                >= notificationWorkerProperties.getSafeMaxRetryCount()) {
            return;
        }

        NotificationOutbox notification = notificationOutboxRepository
                .findById(dispatchedNotification.getId())
                .orElseThrow();

        if (notification.getStatus() != NotificationStatus.FAILED) {
            return;
        }

        OffsetDateTime nextRetryAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(notificationWorkerProperties.getRetryDelay());

        notification.scheduleRetry(nextRetryAt);

        notificationOutboxRepository.save(notification);
    }
}