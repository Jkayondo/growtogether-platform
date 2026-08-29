-- GrowTogether Enterprise Notification Service
-- A12.5a2 — Enterprise Multi-Provider Notification Routing
--
-- Architecture:
--   EIP remains authoritative for external connector identity,
--   endpoint configuration, credentials and certification.
--
--   ENS owns notification-specific channel routing, priority,
--   failover selection and immutable provider-attempt evidence.
--
-- IMPROVEMENT:
--   Composite tenant foreign keys prevent an ENS notification
--   from being routed through another tenant's EIP connector.


-- Composite uniqueness supports tenant-safe foreign keys.
CREATE UNIQUE INDEX IF NOT EXISTS ux_eip_external_connector_tenant_id
    ON eip_external_connectors (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ens_notification_request_tenant_id
    ON ens_notification_requests (tenant_id, id);


CREATE TABLE ens_notification_channel_routes (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    channel VARCHAR(20) NOT NULL,

    connector_id UUID NOT NULL,

    priority INTEGER NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    failover_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_ens_channel_route_connector
        FOREIGN KEY (tenant_id, connector_id)
        REFERENCES eip_external_connectors (tenant_id, id),

    CONSTRAINT uq_ens_channel_route_tenant_id
        UNIQUE (tenant_id, id),

    CONSTRAINT uq_ens_channel_route_connector
        UNIQUE (tenant_id, channel, connector_id),

    CONSTRAINT uq_ens_channel_route_priority
        UNIQUE (tenant_id, channel, priority),

    CONSTRAINT ck_ens_channel_route_priority
        CHECK (priority > 0)
);


CREATE INDEX ix_ens_channel_route_resolution
    ON ens_notification_channel_routes (
        tenant_id,
        channel,
        enabled,
        priority
    );


CREATE TABLE ens_notification_provider_attempts (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    notification_request_id UUID NOT NULL,

    connector_id UUID NOT NULL,

    route_id UUID,

    attempt_number INTEGER NOT NULL,

    attempt_status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    provider_request_id VARCHAR(200),

    provider_reference VARCHAR(200),

    provider_response_code VARCHAR(100),

    provider_response_message TEXT,

    submitted_at TIMESTAMPTZ,

    completed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_ens_provider_attempt_notification
        FOREIGN KEY (tenant_id, notification_request_id)
        REFERENCES ens_notification_requests (tenant_id, id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ens_provider_attempt_connector
        FOREIGN KEY (tenant_id, connector_id)
        REFERENCES eip_external_connectors (tenant_id, id),

    CONSTRAINT fk_ens_provider_attempt_route
        FOREIGN KEY (tenant_id, route_id)
        REFERENCES ens_notification_channel_routes (tenant_id, id),

    CONSTRAINT uq_ens_provider_attempt_number
        UNIQUE (
            tenant_id,
            notification_request_id,
            attempt_number
        ),

    CONSTRAINT ck_ens_provider_attempt_number
        CHECK (attempt_number > 0),

    CONSTRAINT ck_ens_provider_attempt_status
        CHECK (
            attempt_status IN (
                'CREATED',
                'SUBMITTED',
                'ACCEPTED',
                'FAILED',
                'TIMED_OUT'
            )
        )
);


CREATE INDEX ix_ens_provider_attempt_notification
    ON ens_notification_provider_attempts (
        tenant_id,
        notification_request_id,
        attempt_number
    );


CREATE INDEX ix_ens_provider_attempt_connector
    ON ens_notification_provider_attempts (
        tenant_id,
        connector_id,
        created_at
    );


CREATE INDEX ix_ens_provider_attempt_status
    ON ens_notification_provider_attempts (
        tenant_id,
        attempt_status,
        created_at
    );
