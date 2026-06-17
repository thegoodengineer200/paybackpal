package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.notification.config.NotificationWorkerProperties;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class NotificationWorkerService {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerService.class);

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationDispatchService notificationDispatchService;
    private final NotificationRetryService notificationRetryService;
    private final NotificationWorkerProperties notificationWorkerProperties;

    public NotificationWorkerService(
            NotificationOutboxRepository notificationOutboxRepository,
            NotificationDispatchService notificationDispatchService,
            NotificationRetryService notificationRetryService,
            NotificationWorkerProperties notificationWorkerProperties
    ) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.notificationDispatchService = notificationDispatchService;
        this.notificationRetryService = notificationRetryService;
        this.notificationWorkerProperties = notificationWorkerProperties;
    }

    public int processDueNotifications() {
        List<NotificationOutbox> dueNotifications = notificationOutboxRepository
                .findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        NotificationStatus.PENDING,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        PageRequest.of(0, notificationWorkerProperties.getSafeBatchSize())
                );

        int processedCount = 0;

        for (NotificationOutbox notification : dueNotifications) {
            try {
                NotificationOutbox dispatchedNotification = notificationDispatchService.dispatch(notification.getId());
                notificationRetryService.scheduleRetryIfEligible(dispatchedNotification);
                processedCount++;
            } catch (RuntimeException exception) {
                log.warn(
                        "Failed to process notification. notificationId={}, error={}",
                        notification.getId(),
                        exception.getMessage()
                );
            }
        }
        return processedCount;
    }
}