package com.paybackpal.backend.notification.entity;

import com.paybackpal.backend.transaction.entity.TransactionSplit;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_split_id")
    private TransactionSplit transactionSplit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 60)
    private NotificationType notificationType;

    @Column(name = "recipient_phone_number", nullable = false, length = 20)
    private String recipientPhoneNumber;

    @Column(name = "message_body", nullable = false, length = 2000)
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected NotificationOutbox() {}

    public NotificationOutbox(
            TransactionSplit transactionSplit,
            NotificationChannel channel,
            NotificationType notificationType,
            String recipientPhoneNumber,
            String messageBody,
            OffsetDateTime scheduledAt
    ) {
        this.transactionSplit = transactionSplit;
        this.channel = channel;
        this.notificationType = notificationType;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.messageBody = messageBody;
        this.scheduledAt = scheduledAt;
        this.status = NotificationStatus.PENDING;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markProcessing() {
        this.status = NotificationStatus.PROCESSING;
    }

    public void markSent(String providerMessageId) {
        this.status = NotificationStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.failureReason = failureReason;
        this.retryCount++;
    }

    public void cancel() {
        this.status = NotificationStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public TransactionSplit getTransactionSplit() {
        return transactionSplit;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public OffsetDateTime getFailedAt() {
        return failedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}