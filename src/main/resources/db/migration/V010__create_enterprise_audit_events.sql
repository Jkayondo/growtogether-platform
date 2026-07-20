CREATE TABLE eiam_audit_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    actor_user_id UUID NULL,
    actor_username VARCHAR(150) NULL,
    event_type VARCHAR(120) NOT NULL,
    category VARCHAR(40) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    resource_type VARCHAR(100) NULL,
    resource_id VARCHAR(100) NULL,
    source_ip VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    correlation_id VARCHAR(128) NOT NULL,
    session_id UUID NULL,
    message VARCHAR(500) NOT NULL,
    details_json TEXT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_audit_category CHECK (
        category IN (
            'AUTHENTICATION',
            'AUTHORIZATION',
            'IDENTITY',
            'TENANT_ADMINISTRATION',
            'SECURITY',
            'DATA_ACCESS'
        )
    ),
    CONSTRAINT ck_audit_outcome CHECK (
        outcome IN ('SUCCESS', 'FAILURE', 'DENIED')
    ),
    CONSTRAINT ck_audit_severity CHECK (
        severity IN ('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
    )
);

CREATE INDEX ix_audit_tenant_occurred
    ON eiam_audit_events (tenant_id, occurred_at DESC);

CREATE INDEX ix_audit_actor
    ON eiam_audit_events (tenant_id, actor_user_id, occurred_at DESC);

CREATE INDEX ix_audit_event_type
    ON eiam_audit_events (tenant_id, event_type, occurred_at DESC);

CREATE INDEX ix_audit_correlation
    ON eiam_audit_events (correlation_id);

REVOKE UPDATE, DELETE
    ON eiam_audit_events
    FROM PUBLIC;

UPDATE platform_metadata
SET metadata_value = '0.16.0-SNAPSHOT',
    updated_at = NOW()
WHERE metadata_key = 'schema.version';