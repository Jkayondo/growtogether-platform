-- GT Curriculum AI Context Foundation
-- GT-SCHOOL-CURRICULUM-INT-001

CREATE TABLE IF NOT EXISTS curriculum_ai_context (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    curriculum_id UUID NOT NULL,

    curriculum_version_id UUID NOT NULL,

    country_code VARCHAR(10),

    education_system VARCHAR(150),

    education_level_id UUID,

    class_grade_id UUID,

    subject_id UUID,

    learning_outcome_id UUID,

    context_summary VARCHAR(10000),

    context_status VARCHAR(50),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_curriculum_ai_context_tenant
ON curriculum_ai_context(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_curriculum_ai_context_curriculum
ON curriculum_ai_context(
    tenant_id,
    curriculum_id,
    curriculum_version_id
);


CREATE INDEX IF NOT EXISTS ix_curriculum_ai_context_subject
ON curriculum_ai_context(
    tenant_id,
    subject_id
);

