CREATE TABLE IF NOT EXISTS gts_learner_support_recommendation (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    intelligence_snapshot_id UUID,

    risk_level VARCHAR(50),

    recommendation_text TEXT,

    workflow_instance_id UUID,

    recommendation_status VARCHAR(50),

    reviewed_by VARCHAR(255),

    reviewed_at TIMESTAMP,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);
