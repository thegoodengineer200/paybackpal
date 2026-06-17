package com.paybackpal.backend.notification.gateway;

import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationType;

import java.util.UUID;

public class WhatsAppSendRequest {

    private final UUID notificationId;
    private final String recipientPhoneNumber;
    private final String messageBody;
    private final NotificationType notificationType;

    public WhatsAppSendRequest(UUID notificationId, String recipientPhoneNumber, String messageBody, NotificationType notificationType) {
        this.notificationId = notificationId;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.messageBody = messageBody;
        this.notificationType = notificationType;
    }

    public static WhatsAppSendRequest from(NotificationOutbox notification) {
        return new WhatsAppSendRequest(
                notification.getId(),
                notification.getRecipientPhoneNumber(),
                notification.getMessageBody(),
                notification.getNotificationType()
        );
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}