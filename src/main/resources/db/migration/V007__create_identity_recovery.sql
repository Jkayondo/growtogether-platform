ALTER TABLE eiam_user_account ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMPTZ;

CREATE TABLE eiam_recovery_token (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES eiam_user_account(id),
    purpose VARCHAR(32) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    invalidated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX idx_recovery_token_lookup ON eiam_recovery_token(token_hash, purpose);
CREATE INDEX idx_recovery_token_user ON eiam_recovery_token(tenant_id, user_id, purpose);
CREATE INDEX idx_recovery_token_expiry ON eiam_recovery_token(expires_at);
