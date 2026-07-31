CREATE TABLE IF NOT EXISTS parent_engagement_governance_security_events (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    user_id UUID NOT NULL,

    event_type VARCHAR(50) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
