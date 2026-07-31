CREATE TABLE IF NOT EXISTS parent_engagement_privacy_decision_audits (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    parent_id UUID NOT NULL,

    communication_id UUID,

    decision VARCHAR(40) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
