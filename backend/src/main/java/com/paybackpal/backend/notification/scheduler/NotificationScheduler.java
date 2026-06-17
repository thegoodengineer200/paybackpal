package com.paybackpal.backend.notification.scheduler;

import com.paybackpal.backend.notification.config.NotificationWorkerProperties;
import com.paybackpal.backend.notification.service.NotificationWorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationWorkerService notificationWorkerService;
    private final NotificationWorkerProperties notificationWorkerProperties;

    public NotificationScheduler(
            NotificationWorkerService notificationWorkerService,
            NotificationWorkerProperties notificationWorkerProperties
    ) {
        this.notificationWorkerService = notificationWorkerService;
        this.notificationWorkerProperties = notificationWorkerProperties;
    }

    @Scheduled(fixedDelayString = "${app.notifications.poll-interval-ms:10000}")
    public void processDueNotifications() {
        if (!notificationWorkerProperties.isSchedulerEnabled()) return;
        int processedCount = notificationWorkerService.processDueNotifications();
        if (processedCount > 0) {
            log.info("Processed due notifications. count={}", processedCount);
        }
    }
}