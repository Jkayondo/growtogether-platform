CREATE TABLE IF NOT EXISTS parent_notification_processing_events (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    notification_id UUID NOT NULL,

    processing_status VARCHAR(30) NOT NULL,

    processed_at TIMESTAMP,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
