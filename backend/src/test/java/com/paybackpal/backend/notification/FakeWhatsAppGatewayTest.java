package com.paybackpal.backend.notification;

import com.paybackpal.backend.notification.entity.NotificationType;
import com.paybackpal.backend.notification.gateway.FakeWhatsAppGateway;
import com.paybackpal.backend.notification.gateway.WhatsAppSendRequest;
import com.paybackpal.backend.notification.gateway.WhatsAppSendResult;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FakeWhatsAppGatewayTest {

    @Test
    void sendShouldReturnSuccessfulFakeProviderMessageId() {
        FakeWhatsAppGateway gateway = new FakeWhatsAppGateway();

        WhatsAppSendRequest request = new WhatsAppSendRequest(
                UUID.randomUUID(),
                "9876500000",
                "Test WhatsApp message",
                NotificationType.INITIAL_PAYMENT_REQUEST
        );

        WhatsAppSendResult result = gateway.send(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderMessageId()).startsWith("fake-whatsapp-");
        assertThat(result.getFailureReason()).isNull();
    }
}