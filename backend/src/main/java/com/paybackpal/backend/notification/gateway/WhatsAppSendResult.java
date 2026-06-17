package com.paybackpal.backend.notification.gateway;

public class WhatsAppSendResult {

    private final boolean success;
    private final String providerMessageId;
    private final String failureReason;

    private WhatsAppSendResult(
            boolean success,
            String providerMessageId,
            String failureReason
    ) {
        this.success = success;
        this.providerMessageId = providerMessageId;
        this.failureReason = failureReason;
    }

    public static WhatsAppSendResult sent(String providerMessageId) {
        return new WhatsAppSendResult(true, providerMessageId, null);
    }

    public static WhatsAppSendResult failed(String failureReason) {
        return new WhatsAppSendResult(false, null, failureReason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}