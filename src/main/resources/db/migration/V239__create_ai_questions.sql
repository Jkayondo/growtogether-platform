-- GT Assessment AI Question Foundation

CREATE TABLE IF NOT EXISTS ai_questions (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    question_text VARCHAR(5000) NOT NULL,

    question_type VARCHAR(50) NOT NULL,

    difficulty VARCHAR(50) NOT NULL,

    subject_id UUID,

    curriculum_version_id UUID,

    learning_outcome_id UUID,

    generated_by VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT ck_ai_question_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);


CREATE INDEX IF NOT EXISTS ix_ai_questions_tenant
ON ai_questions(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_ai_questions_subject
ON ai_questions(
    tenant_id,
    subject_id
);


CREATE INDEX IF NOT EXISTS ix_ai_questions_curriculum
ON ai_questions(
    tenant_id,
    curriculum_version_id
);

