package com.paybackpal.backend.notification.service;

import com.paybackpal.backend.common.exception.BusinessRuleViolationException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.notification.entity.NotificationChannel;
import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import com.paybackpal.backend.notification.gateway.WhatsAppGateway;
import com.paybackpal.backend.notification.gateway.WhatsAppSendRequest;
import com.paybackpal.backend.notification.gateway.WhatsAppSendResult;
import com.paybackpal.backend.notification.repository.NotificationOutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class NotificationDispatchService {

    private static final int MAX_FAILURE_REASON_LENGTH = 1000;

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final WhatsAppGateway whatsAppGateway;

    public NotificationDispatchService(
            NotificationOutboxRepository notificationOutboxRepository, WhatsAppGateway whatsAppGateway) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.whatsAppGateway = whatsAppGateway;
    }

    @Transactional
    public NotificationOutbox dispatch(UUID notificationId) {
        NotificationOutbox notification = notificationOutboxRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        validateDispatchable(notification);
        notification.markProcessing();
        notificationOutboxRepository.saveAndFlush(notification);

        try {
            WhatsAppSendResult result = whatsAppGateway.send(
                    WhatsAppSendRequest.from(notification)
            );

            if (result.isSuccess()) {
                notification.markSent(result.getProviderMessageId());
            } else {
                notification.markFailed(
                        normalizeFailureReason(result.getFailureReason())
                );
            }
        } catch (RuntimeException exception) {
            notification.markFailed(
                    normalizeFailureReason(exception.getMessage())
            );
        }

        return notificationOutboxRepository.save(notification);
    }

    private void validateDispatchable(NotificationOutbox notification) {
        if (notification.getChannel() != NotificationChannel.WHATSAPP) {
            throw new BusinessRuleViolationException(
                    "Only WhatsApp notifications can be dispatched by this service"
            );
        }

        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Only pending notifications can be dispatched"
            );
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (notification.getScheduledAt().isAfter(now)) {
            throw new BusinessRuleViolationException(
                    "Notification is scheduled for the future"
            );
        }
    }

    private String normalizeFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Unknown WhatsApp gateway failure";
        }
        String trimmedFailureReason = failureReason.trim();
        if (trimmedFailureReason.length() <= MAX_FAILURE_REASON_LENGTH) {
            return trimmedFailureReason;
        }
        return trimmedFailureReason.substring(0, MAX_FAILURE_REASON_LENGTH);
    }
}