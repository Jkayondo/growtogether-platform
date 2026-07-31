CREATE TABLE IF NOT EXISTS parent_engagement_communication_audits (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    recipient_id UUID NOT NULL,

    report_id UUID,

    action VARCHAR(40) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_parent_engagement_communication_audit_tenant
ON parent_engagement_communication_audits (tenant_id);
