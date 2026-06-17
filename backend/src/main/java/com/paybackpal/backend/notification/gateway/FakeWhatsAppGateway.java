package com.paybackpal.backend.notification.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnProperty(
        prefix = "app.whatsapp",
        name = "provider",
        havingValue = "fake",
        matchIfMissing = true
)
public class FakeWhatsAppGateway implements WhatsAppGateway {

    private static final Logger log = LoggerFactory.getLogger(FakeWhatsAppGateway.class);

    @Override
    public WhatsAppSendResult send(WhatsAppSendRequest request) {
        String providerMessageId = "fake-whatsapp-" + UUID.randomUUID();
        log.info(
                "Fake WhatsApp sent. notificationId={}, recipientPhoneNumber={}, notificationType={}, providerMessageId={}, messageLength={}",
                request.getNotificationId(),
                request.getRecipientPhoneNumber(),
                request.getNotificationType(),
                providerMessageId,
                request.getMessageBody() == null ? 0 : request.getMessageBody().length()
        );
        return WhatsAppSendResult.sent(providerMessageId);
    }
}