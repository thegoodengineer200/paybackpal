package com.paybackpal.backend.notification;

import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.gateway.WhatsAppGateway;
import com.paybackpal.backend.notification.gateway.WhatsAppSendRequest;
import com.paybackpal.backend.notification.gateway.WhatsAppSendResult;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import com.paybackpal.backend.notification.service.NotificationDispatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    @Mock
    private WhatsAppGateway whatsAppGateway;

    @Test
    void dispatchShouldSendPendingWhatsAppNotificationAndMarkSent() {
        NotificationDispatchService service = new NotificationDispatchService(
                notificationOutboxRepository,
                whatsAppGateway
        );

        UUID notificationId = UUID.randomUUID();

        NotificationOutbox notification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.INITIAL_PAYMENT_REQUEST,
                "9876500000",
                "Please pay your share",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        when(notificationOutboxRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        when(notificationOutboxRepository.saveAndFlush(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(notificationOutboxRepository.save(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(whatsAppGateway.send(any(WhatsAppSendRequest.class)))
                .thenReturn(WhatsAppSendResult.sent("provider-message-123"));

        NotificationOutbox dispatchedNotification = service.dispatch(notificationId);

        assertThat(dispatchedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(dispatchedNotification.getProviderMessageId()).isEqualTo("provider-message-123");
        assertThat(dispatchedNotification.getSentAt()).isNotNull();
        assertThat(dispatchedNotification.getFailureReason()).isNull();

        ArgumentCaptor<WhatsAppSendRequest> requestCaptor =
                ArgumentCaptor.forClass(WhatsAppSendRequest.class);

        verify(whatsAppGateway).send(requestCaptor.capture());

        WhatsAppSendRequest request = requestCaptor.getValue();

        assertThat(request.getRecipientPhoneNumber()).isEqualTo("9876500000");
        assertThat(request.getMessageBody()).isEqualTo("Please pay your share");
        assertThat(request.getNotificationType()).isEqualTo(NotificationType.INITIAL_PAYMENT_REQUEST);

        verify(notificationOutboxRepository).saveAndFlush(notification);
        verify(notificationOutboxRepository).save(notification);
    }

    @Test
    void dispatchShouldMarkFailedWhenGatewayReturnsFailure() {
        NotificationDispatchService service = new NotificationDispatchService(
                notificationOutboxRepository,
                whatsAppGateway
        );

        UUID notificationId = UUID.randomUUID();

        NotificationOutbox notification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500000",
                "Manual reminder",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        when(notificationOutboxRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        when(notificationOutboxRepository.saveAndFlush(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(notificationOutboxRepository.save(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(whatsAppGateway.send(any(WhatsAppSendRequest.class)))
                .thenReturn(WhatsAppSendResult.failed("Provider unavailable"));

        NotificationOutbox dispatchedNotification = service.dispatch(notificationId);

        assertThat(dispatchedNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(dispatchedNotification.getRetryCount()).isEqualTo(1);
        assertThat(dispatchedNotification.getFailedAt()).isNotNull();
        assertThat(dispatchedNotification.getFailureReason()).isEqualTo("Provider unavailable");
        assertThat(dispatchedNotification.getProviderMessageId()).isNull();
    }

    @Test
    void dispatchShouldMarkFailedWhenGatewayThrowsException() {
        NotificationDispatchService service = new NotificationDispatchService(
                notificationOutboxRepository,
                whatsAppGateway
        );

        UUID notificationId = UUID.randomUUID();

        NotificationOutbox notification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500000",
                "Manual reminder",
                OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1)
        );

        when(notificationOutboxRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        when(notificationOutboxRepository.saveAndFlush(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(notificationOutboxRepository.save(any(NotificationOutbox.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(whatsAppGateway.send(any(WhatsAppSendRequest.class)))
                .thenThrow(new RuntimeException("Gateway timeout"));

        NotificationOutbox dispatchedNotification = service.dispatch(notificationId);

        assertThat(dispatchedNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(dispatchedNotification.getRetryCount()).isEqualTo(1);
        assertThat(dispatchedNotification.getFailedAt()).isNotNull();
        assertThat(dispatchedNotification.getFailureReason()).isEqualTo("Gateway timeout");
    }

    @Test
    void dispatchShouldRejectFutureScheduledNotification() {
        NotificationDispatchService service = new NotificationDispatchService(
                notificationOutboxRepository,
                whatsAppGateway
        );

        UUID notificationId = UUID.randomUUID();

        NotificationOutbox notification = new NotificationOutbox(
                null,
                NotificationChannel.WHATSAPP,
                NotificationType.MANUAL_REMINDER,
                "9876500000",
                "Future reminder",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1)
        );

        when(notificationOutboxRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.dispatch(notificationId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("scheduled for the future");

        verifyNoInteractions(whatsAppGateway);
        verify(notificationOutboxRepository, never()).save(any());
        verify(notificationOutboxRepository, never()).saveAndFlush(any());
    }
}