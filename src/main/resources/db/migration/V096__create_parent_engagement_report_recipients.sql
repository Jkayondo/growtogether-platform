CREATE TABLE IF NOT EXISTS parent_engagement_report_recipients (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    user_id UUID NOT NULL,

    recipient_type VARCHAR(40) NOT NULL,

    delivery_channel VARCHAR(40) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
