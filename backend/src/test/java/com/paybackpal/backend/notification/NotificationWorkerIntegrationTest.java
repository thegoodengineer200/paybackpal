package com.paybackpal.backend.notification;

import com.paybackpal.backend.BaseIntegrationTest;
import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.notification.service.NotificationWorkerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "app.whatsapp.provider=fake",
        "app.notifications.scheduler-enabled=false",
        "app.notifications.batch-size=10",
        "app.notifications.max-retry-count=3",
        "app.notifications.retry-delay-seconds=300"
})
class NotificationWorkerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationOutboxRepository notificationOutboxRepository;

    @Autowired
    private NotificationWorkerService notificationWorkerService;

    @Test
    void processDueNotificationsShouldSendDuePendingNotifications() {
        NotificationOutbox dueNotification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500000",
                "Due message",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        NotificationOutbox futureNotification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500001",
                "Future message",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1)
        );

        NotificationOutbox savedDueNotification = notificationOutboxRepository.save(dueNotification);
        NotificationOutbox savedFutureNotification = notificationOutboxRepository.save(futureNotification);

        int processedCount = notificationWorkerService.processDueNotifications();

        assertThat(processedCount).isEqualTo(1);

        NotificationOutbox reloadedDueNotification = notificationOutboxRepository
                .findById(savedDueNotification.getId())
                .orElseThrow();

        NotificationOutbox reloadedFutureNotification = notificationOutboxRepository
                .findById(savedFutureNotification.getId())
                .orElseThrow();

        assertThat(reloadedDueNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(reloadedDueNotification.getProviderMessageId()).startsWith("fake-whatsapp-");
        assertThat(reloadedDueNotification.getSentAt()).isNotNull();

        assertThat(reloadedFutureNotification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(reloadedFutureNotification.getProviderMessageId()).isNull();
        assertThat(reloadedFutureNotification.getSentAt()).isNull();
    }
}