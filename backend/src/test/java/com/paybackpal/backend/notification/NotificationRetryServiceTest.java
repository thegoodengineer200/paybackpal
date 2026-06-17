package com.paybackpal.backend.notification;

import com.paybackpal.backend.notification.config.NotificationWorkerProperties;
import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.notification.service.NotificationRetryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRetryServiceTest {

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    @Test
    void scheduleRetryIfEligibleShouldMoveFailedNotificationBackToPending() {
        NotificationWorkerProperties properties = new NotificationWorkerProperties();
        properties.setMaxRetryCount(3);
        properties.setRetryDelaySeconds(300);

        NotificationRetryService service = new NotificationRetryService(
                notificationOutboxRepository,
                properties
        );

        NotificationOutbox notification = createNotification();
        notification.markFailed("Provider unavailable");

        when(notificationOutboxRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        service.scheduleRetryIfEligible(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getScheduledAt())
                .isAfter(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(250));

        verify(notificationOutboxRepository).save(notification);
    }

    @Test
    void scheduleRetryIfEligibleShouldNotRetryWhenMaxRetryCountReached() {
        NotificationWorkerProperties properties = new NotificationWorkerProperties();
        properties.setMaxRetryCount(1);
        properties.setRetryDelaySeconds(300);

        NotificationRetryService service = new NotificationRetryService(
                notificationOutboxRepository,
                properties
        );

        NotificationOutbox notification = createNotification();
        notification.markFailed("Provider unavailable");

        service.scheduleRetryIfEligible(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getRetryCount()).isEqualTo(1);

        verifyNoInteractions(notificationOutboxRepository);
    }

    @Test
    void scheduleRetryIfEligibleShouldIgnoreSentNotification() {
        NotificationWorkerProperties properties = new NotificationWorkerProperties();

        NotificationRetryService service = new NotificationRetryService(
                notificationOutboxRepository,
                properties
        );

        NotificationOutbox notification = createNotification();
        notification.markSent("provider-message-123");

        service.scheduleRetryIfEligible(notification);

        verifyNoInteractions(notificationOutboxRepository);
    }

    private NotificationOutbox createNotification() {
        NotificationOutbox notification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500000",
                "Manual reminder",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        setIdForTest(notification, UUID.randomUUID());

        return notification;
    }

    private void setIdForTest(NotificationOutbox notification, UUID id) {
        try {
            var field = NotificationOutbox.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(notification, id);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}