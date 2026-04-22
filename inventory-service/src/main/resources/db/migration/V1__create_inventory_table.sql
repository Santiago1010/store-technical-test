CREATE TABLE inventory (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  UUID        NOT NULL UNIQUE,
    available   INTEGER     NOT NULL CHECK (available >= 0),
    version     BIGINT      NOT NULL DEFAULT 0
);
