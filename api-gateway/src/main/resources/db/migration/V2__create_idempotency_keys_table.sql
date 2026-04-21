CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users(id),
    endpoint        VARCHAR(255) NOT NULL,
    response_status INTEGER      NOT NULL,
    response_body   TEXT         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_idempotency_user ON idempotency_keys(user_id);