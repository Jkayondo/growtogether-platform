CREATE TABLE eiam_mfa_profile (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, user_id UUID NOT NULL,
 encrypted_totp_secret VARCHAR(500) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT FALSE,
 enrolled_at TIMESTAMPTZ, last_verified_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uq_mfa_profile_tenant_user UNIQUE (tenant_id,user_id),
 CONSTRAINT fk_mfa_profile_user FOREIGN KEY (user_id) REFERENCES eiam_user_account(id)
);
CREATE INDEX ix_mfa_profile_user ON eiam_mfa_profile(tenant_id,user_id);

CREATE TABLE eiam_mfa_recovery_code (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, user_id UUID NOT NULL,
 code_hash VARCHAR(64) NOT NULL, used_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uq_mfa_recovery_hash UNIQUE (tenant_id,code_hash),
 CONSTRAINT fk_mfa_recovery_user FOREIGN KEY (user_id) REFERENCES eiam_user_account(id)
);
CREATE INDEX ix_mfa_recovery_user ON eiam_mfa_recovery_code(tenant_id,user_id);

CREATE TABLE eiam_mfa_challenge (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, user_id UUID NOT NULL,
 challenge_hash VARCHAR(64) NOT NULL, expires_at TIMESTAMPTZ NOT NULL, consumed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uq_mfa_challenge_hash UNIQUE (challenge_hash),
 CONSTRAINT fk_mfa_challenge_user FOREIGN KEY (user_id) REFERENCES eiam_user_account(id)
);
CREATE INDEX ix_mfa_challenge_expiry ON eiam_mfa_challenge(expires_at);

CREATE TABLE eiam_trusted_device (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, user_id UUID NOT NULL,
 token_hash VARCHAR(64) NOT NULL, device_name VARCHAR(150) NOT NULL,
 fingerprint_hash VARCHAR(64) NOT NULL, expires_at TIMESTAMPTZ NOT NULL,
 last_used_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uq_trusted_device_token UNIQUE (token_hash),
 CONSTRAINT fk_trusted_device_user FOREIGN KEY (user_id) REFERENCES eiam_user_account(id)
);
CREATE INDEX ix_trusted_device_user ON eiam_trusted_device(tenant_id,user_id);

ALTER TABLE eiam_user_session ADD COLUMN mfa_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE eiam_user_session ADD COLUMN authentication_assurance VARCHAR(20) NOT NULL DEFAULT 'AAL1';
ALTER TABLE eiam_user_session ADD COLUMN trusted_device BOOLEAN NOT NULL DEFAULT FALSE;
