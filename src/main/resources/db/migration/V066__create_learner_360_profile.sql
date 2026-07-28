CREATE TABLE gts_learner_360_profile (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    academic_profile_id UUID NOT NULL,

    current_class_grade_id UUID,

    current_curriculum_version_id UUID,

    overall_performance_level VARCHAR(50),

    competency_progress_level VARCHAR(50),

    learning_risk_level VARCHAR(50),

    growth_summary TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);
