CREATE TABLE IF NOT EXISTS parent_academic_notifications (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    parent_id UUID NOT NULL,

    learner_id UUID,

    notification_type VARCHAR(50) NOT NULL,

    message VARCHAR(500) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_parent_academic_notification_tenant
ON parent_academic_notifications (tenant_id);
