CREATE TABLE IF NOT EXISTS parent_academic_audit_events (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    parent_id UUID NOT NULL,

    learner_id UUID,

    event_type VARCHAR(40) NOT NULL,

    event_time TIMESTAMP NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_parent_academic_audit_tenant
ON parent_academic_audit_events (tenant_id);
