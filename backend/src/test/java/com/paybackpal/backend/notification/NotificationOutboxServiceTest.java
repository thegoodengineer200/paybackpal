package com.paybackpal.backend.notification;

import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.notification.service.NotificationOutboxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxServiceTest {

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    @Test
    void enqueueWhatsAppShouldCreatePendingWhatsAppNotification() {

        NotificationOutboxService service = new NotificationOutboxService(notificationOutboxRepository);
        OffsetDateTime scheduledAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5);

        when(notificationOutboxRepository.save(any(NotificationOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationOutbox saved = service.enqueueWhatsApp(
                null,
                NotificationType.INITIAL_PAYMENT_REQUEST,
                "9876500000",
                "Please pay your share",
                scheduledAt
        );

        ArgumentCaptor<NotificationOutbox> captor = ArgumentCaptor.forClass(NotificationOutbox.class);
        verify(notificationOutboxRepository).save(captor.capture());
        NotificationOutbox notification = captor.getValue();

        assertThat(saved).isSameAs(notification);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.WHATSAPP);
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.INITIAL_PAYMENT_REQUEST);
        assertThat(notification.getRecipientPhoneNumber()).isEqualTo("9876500000");
        assertThat(notification.getMessageBody()).isEqualTo("Please pay your share");
        assertThat(notification.getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isZero();
    }
}