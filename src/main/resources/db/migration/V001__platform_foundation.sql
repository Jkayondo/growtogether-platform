CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE platform_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metadata_key VARCHAR(100) NOT NULL UNIQUE,
    metadata_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO platform_metadata (metadata_key, metadata_value)
VALUES ('schema.version', '001');
