CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    transaction_split_id UUID,

    channel VARCHAR(30) NOT NULL,
    notification_type VARCHAR(60) NOT NULL,
    recipient_phone_number VARCHAR(20) NOT NULL,
    message_body VARCHAR(2000) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    scheduled_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,

    retry_count INTEGER NOT NULL DEFAULT 0,
    provider_message_id VARCHAR(255),
    failure_reason VARCHAR(1000),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notification_outbox_transaction_split
        FOREIGN KEY (transaction_split_id)
        REFERENCES transaction_splits(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_notification_outbox_retry_count_non_negative
        CHECK (retry_count >= 0)
);

CREATE INDEX idx_notification_outbox_status_scheduled_at
    ON notification_outbox(status, scheduled_at);

CREATE INDEX idx_notification_outbox_transaction_split_id
    ON notification_outbox(transaction_split_id);

CREATE INDEX idx_notification_outbox_recipient_phone_number
    ON notification_outbox(recipient_phone_number);