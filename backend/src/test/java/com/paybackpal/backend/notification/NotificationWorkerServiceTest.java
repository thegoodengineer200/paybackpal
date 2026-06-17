package com.paybackpal.backend.notification;

import com.paybackpal.backend.notification.config.NotificationWorkerProperties;
import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.notification.service.NotificationDispatchService;
import com.paybackpal.backend.notification.service.NotificationRetryService;
import com.paybackpal.backend.notification.service.NotificationWorkerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerServiceTest {

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    @Mock
    private NotificationRetryService notificationRetryService;

    @Test
    void processDueNotificationsShouldDispatchEachDueNotification() {
        NotificationWorkerProperties properties = new NotificationWorkerProperties();
        properties.setBatchSize(10);

        NotificationWorkerService service = new NotificationWorkerService(
                notificationOutboxRepository,
                notificationDispatchService,
                notificationRetryService,
                properties
        );

        NotificationOutbox notificationOne = createNotification();
        NotificationOutbox notificationTwo = createNotification();

        when(notificationOutboxRepository.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                eq(NotificationStatus.PENDING),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(notificationOne, notificationTwo));

        when(notificationDispatchService.dispatch(notificationOne.getId()))
                .thenReturn(notificationOne);

        when(notificationDispatchService.dispatch(notificationTwo.getId()))
                .thenReturn(notificationTwo);

        int processedCount = service.processDueNotifications();

        assertThat(processedCount).isEqualTo(2);

        verify(notificationDispatchService).dispatch(notificationOne.getId());
        verify(notificationDispatchService).dispatch(notificationTwo.getId());

        verify(notificationRetryService).scheduleRetryIfEligible(notificationOne);
        verify(notificationRetryService).scheduleRetryIfEligible(notificationTwo);
    }

    @Test
    void processDueNotificationsShouldContinueWhenOneNotificationFails() {
        NotificationWorkerProperties properties = new NotificationWorkerProperties();

        NotificationWorkerService service = new NotificationWorkerService(
                notificationOutboxRepository,
                notificationDispatchService,
                notificationRetryService,
                properties
        );

        NotificationOutbox notificationOne = createNotification();
        NotificationOutbox notificationTwo = createNotification();

        when(notificationOutboxRepository.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                eq(NotificationStatus.PENDING),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(notificationOne, notificationTwo));

        when(notificationDispatchService.dispatch(notificationOne.getId()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        when(notificationDispatchService.dispatch(notificationTwo.getId()))
                .thenReturn(notificationTwo);

        int processedCount = service.processDueNotifications();

        assertThat(processedCount).isEqualTo(1);

        verify(notificationDispatchService).dispatch(notificationOne.getId());
        verify(notificationDispatchService).dispatch(notificationTwo.getId());

        verify(notificationRetryService, never()).scheduleRetryIfEligible(notificationOne);
        verify(notificationRetryService).scheduleRetryIfEligible(notificationTwo);
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