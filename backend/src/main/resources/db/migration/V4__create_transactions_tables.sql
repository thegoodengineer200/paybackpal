CREATE TABLE card_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,
    credit_card_id UUID NOT NULL,

    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(255),
    merchant_name VARCHAR(150),
    transaction_date DATE NOT NULL,

    is_borrowed BOOLEAN NOT NULL DEFAULT FALSE,
    owner_share_amount NUMERIC(12, 2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_card_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_card_transactions_credit_card
        FOREIGN KEY (credit_card_id)
        REFERENCES credit_cards(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_card_transactions_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_card_transactions_owner_share_non_negative
        CHECK (owner_share_amount >= 0),

    CONSTRAINT chk_card_transactions_owner_share_not_more_than_amount
        CHECK (owner_share_amount <= amount)
);

CREATE INDEX idx_card_transactions_user_id
    ON card_transactions(user_id);

CREATE INDEX idx_card_transactions_credit_card_id
    ON card_transactions(credit_card_id);

CREATE INDEX idx_card_transactions_card_date
    ON card_transactions(credit_card_id, transaction_date DESC);

CREATE TABLE transaction_splits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    transaction_id UUID NOT NULL,
    borrower_id UUID NOT NULL,

    split_percentage NUMERIC(5, 2),
    split_amount NUMERIC(12, 2) NOT NULL,

    repayment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_transaction_splits_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES card_transactions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_transaction_splits_borrower
        FOREIGN KEY (borrower_id)
        REFERENCES borrowers(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_transaction_splits_percentage_range
        CHECK (split_percentage IS NULL OR split_percentage > 0 AND split_percentage <= 100),

    CONSTRAINT chk_transaction_splits_amount_positive
        CHECK (split_amount > 0)
);

CREATE INDEX idx_transaction_splits_transaction_id
    ON transaction_splits(transaction_id);

CREATE INDEX idx_transaction_splits_borrower_id
    ON transaction_splits(borrower_id);