CREATE TABLE IF NOT EXISTS parent_notification_deliveries (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    notification_id UUID NOT NULL,

    delivery_status VARCHAR(30) NOT NULL,

    channel VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_parent_notification_delivery_tenant
ON parent_notification_deliveries(tenant_id);
