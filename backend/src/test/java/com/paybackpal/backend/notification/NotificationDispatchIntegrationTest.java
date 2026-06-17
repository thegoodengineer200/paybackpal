package com.paybackpal.backend.notification;

import com.paybackpal.backend.BaseIntegrationTest;
import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.notification.service.NotificationDispatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "app.whatsapp.provider=fake"
})
class NotificationDispatchIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationOutboxRepository notificationOutboxRepository;

    @Autowired
    private NotificationDispatchService notificationDispatchService;

    @Test
    void dispatchShouldUseFakeGatewayAndMarkNotificationSent() {
        NotificationOutbox notification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500000",
                "This is a fake WhatsApp message",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        NotificationOutbox savedNotification = notificationOutboxRepository.save(notification);
        NotificationOutbox dispatchedNotification = notificationDispatchService.dispatch(
                savedNotification.getId()
        );
        assertThat(dispatchedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(dispatchedNotification.getProviderMessageId()).startsWith("fake-whatsapp-");
        assertThat(dispatchedNotification.getSentAt()).isNotNull();
        assertThat(dispatchedNotification.getFailureReason()).isNull();
        assertThat(dispatchedNotification.getRetryCount()).isZero();
    }
}