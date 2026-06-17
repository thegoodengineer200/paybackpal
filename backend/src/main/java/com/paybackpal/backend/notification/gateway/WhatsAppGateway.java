package com.paybackpal.backend.notification.gateway;

public interface WhatsAppGateway {

    WhatsAppSendResult send(WhatsAppSendRequest request);
}