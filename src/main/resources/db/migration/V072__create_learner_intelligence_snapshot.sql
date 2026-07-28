CREATE TABLE gts_learner_intelligence_snapshot (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    achievement_status VARCHAR(50),

    risk_level VARCHAR(50),

    support_required BOOLEAN,

    recommendation_summary TEXT,

    calculated_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);
