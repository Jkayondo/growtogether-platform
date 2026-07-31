CREATE TABLE IF NOT EXISTS parent_notification_rules (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    rule_type VARCHAR(50) NOT NULL,

    enabled BOOLEAN NOT NULL,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
