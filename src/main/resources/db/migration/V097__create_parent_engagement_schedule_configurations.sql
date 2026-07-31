CREATE TABLE IF NOT EXISTS parent_engagement_schedule_configurations (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    report_type VARCHAR(50) NOT NULL,

    frequency VARCHAR(30) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
