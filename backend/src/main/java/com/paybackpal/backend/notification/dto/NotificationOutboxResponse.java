package com.paybackpal.backend.notification.dto;

import com.paybackpal.backend.notification.entity.NotificationOutbox;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NotificationOutboxResponse {

    private final UUID id;
    private final UUID transactionSplitId;
    private final String channel;
    private final String notificationType;
    private final String recipientPhoneNumber;
    private final String messageBody;
    private final String status;
    private final OffsetDateTime scheduledAt;
    private final int retryCount;

    public NotificationOutboxResponse(
            UUID id,
            UUID transactionSplitId,
            String channel,
            String notificationType,
            String recipientPhoneNumber,
            String messageBody,
            String status,
            OffsetDateTime scheduledAt,
            int retryCount
    ) {
        this.id = id;
        this.transactionSplitId = transactionSplitId;
        this.channel = channel;
        this.notificationType = notificationType;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.messageBody = messageBody;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.retryCount = retryCount;
    }

    public static NotificationOutboxResponse from(NotificationOutbox notification) {
        UUID splitId = notification.getTransactionSplit() == null ? null : notification.getTransactionSplit().getId();
        return new NotificationOutboxResponse(
                notification.getId(),
                splitId,
                notification.getChannel().name(),
                notification.getNotificationType().name(),
                notification.getRecipientPhoneNumber(),
                notification.getMessageBody(),
                notification.getStatus().name(),
                notification.getScheduledAt(),
                notification.getRetryCount()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionSplitId() {
        return transactionSplitId;
    }

    public String getChannel() {
        return channel;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public int getRetryCount() {
        return retryCount;
    }
}