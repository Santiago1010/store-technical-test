CREATE TABLE idempotency_keys (
    idempotency_key UUID        PRIMARY KEY,
    response_body   TEXT        NOT NULL,
    http_status     INTEGER     NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT now()
);
