CREATE TABLE gts_learner_360_summary (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    learner_360_profile_id UUID NOT NULL,

    overall_score DOUBLE PRECISION,

    competency_completion_percentage DOUBLE PRECISION,

    assessment_count INTEGER,

    support_required BOOLEAN,

    risk_level VARCHAR(50),

    recommendation_summary TEXT,

    calculated_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);
