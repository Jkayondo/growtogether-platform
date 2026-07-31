CREATE TABLE IF NOT EXISTS parent_engagement_scheduled_reports (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    frequency VARCHAR(30) NOT NULL,

    report_type VARCHAR(50) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    last_generated_at TIMESTAMP,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
