CREATE TABLE IF NOT EXISTS parent_engagement_scheduled_deliveries (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    scheduled_report_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
