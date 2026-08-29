-- ENS secure transient notification payload foundation.
--
-- Purpose:
-- Protect short-lived sensitive notification content such as account
-- activation links/tokens from being persisted in the ordinary
-- ens_notification_requests.body column.
--
-- The ordinary notification request retains only safe, non-secret text.
-- The encrypted final provider body is resolved only at dispatch time.
--
-- Payloads remain available for legitimate ENS retries/failover until
-- expiry or explicit retirement.

ALTER TABLE ens_notification_requests
    ADD CONSTRAINT uq_ens_notification_requests_tenant_id
        UNIQUE (tenant_id, id);

CREATE TABLE ens_notification_secure_payloads (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    notification_request_id UUID NOT NULL,

    payload_kind VARCHAR(40) NOT NULL
        DEFAULT 'FINAL_PROVIDER_BODY',

    encrypted_payload TEXT NOT NULL,

    encryption_iv VARCHAR(100) NOT NULL,

    encryption_key_id VARCHAR(100) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,

    retired_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL
        DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_ens_secure_payload_notification_tenant
        FOREIGN KEY (
            tenant_id,
            notification_request_id
        )
        REFERENCES ens_notification_requests (
            tenant_id,
            id
        ),

    CONSTRAINT uq_ens_secure_payload_notification
        UNIQUE (
            tenant_id,
            notification_request_id
        ),

    CONSTRAINT ck_ens_secure_payload_kind
        CHECK (
            payload_kind = 'FINAL_PROVIDER_BODY'
        ),

    CONSTRAINT ck_ens_secure_payload_encrypted
        CHECK (
            LENGTH(TRIM(encrypted_payload)) > 0
            AND LENGTH(TRIM(encryption_iv)) > 0
            AND LENGTH(TRIM(encryption_key_id)) > 0
        ),

    CONSTRAINT ck_ens_secure_payload_expiry
        CHECK (
            expires_at > created_at
        ),

    CONSTRAINT ck_ens_secure_payload_retirement
        CHECK (
            retired_at IS NULL
            OR retired_at >= created_at
        )
);

CREATE INDEX ix_ens_secure_payload_tenant_expiry
    ON ens_notification_secure_payloads (
        tenant_id,
        expires_at
    )
    WHERE retired_at IS NULL;

CREATE INDEX ix_ens_secure_payload_notification
    ON ens_notification_secure_payloads (
        tenant_id,
        notification_request_id
    );
