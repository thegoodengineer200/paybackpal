CREATE TABLE borrower_action_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    transaction_split_id UUID NOT NULL,

    action_type VARCHAR(50) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_borrower_action_tokens_transaction_split
        FOREIGN KEY (transaction_split_id)
        REFERENCES transaction_splits(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_borrower_action_tokens_token_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_borrower_action_tokens_token_hash
    ON borrower_action_tokens(token_hash);

CREATE INDEX idx_borrower_action_tokens_split_action
    ON borrower_action_tokens(transaction_split_id, action_type);

CREATE INDEX idx_borrower_action_tokens_expires_at
    ON borrower_action_tokens(expires_at);