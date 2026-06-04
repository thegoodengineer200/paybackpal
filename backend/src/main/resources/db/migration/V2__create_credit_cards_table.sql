CREATE TABLE credit_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    card_name VARCHAR(100) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    last_four_digits VARCHAR(4) NOT NULL,

    billing_cycle_day INTEGER NOT NULL,
    due_day INTEGER NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_credit_cards_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_credit_cards_last_four_digits
        CHECK (last_four_digits ~ '^[0-9]{4}$'),

    CONSTRAINT chk_credit_cards_billing_cycle_day
        CHECK (billing_cycle_day BETWEEN 1 AND 31),

    CONSTRAINT chk_credit_cards_due_day
        CHECK (due_day BETWEEN 1 AND 31)
);

CREATE INDEX idx_credit_cards_user_id
    ON credit_cards(user_id);

CREATE INDEX idx_credit_cards_user_active
    ON credit_cards(user_id, is_active);