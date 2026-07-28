CREATE TABLE gts_learner_academic_profile (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    current_academic_year_id UUID,

    current_class_grade_id UUID,

    current_curriculum_version_id UUID,

    academic_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    learning_stage VARCHAR(50),

    profile_summary TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);
