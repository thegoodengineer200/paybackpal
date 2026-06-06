CREATE TABLE borrowers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_user_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_borrowers_owner_user
        FOREIGN KEY (owner_user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_borrowers_owner_user_id
    ON borrowers(owner_user_id);

CREATE INDEX idx_borrowers_owner_active
    ON borrowers(owner_user_id, is_active);

CREATE UNIQUE INDEX uk_borrowers_owner_phone_active
    ON borrowers(owner_user_id, phone_number)
    WHERE is_active = TRUE;