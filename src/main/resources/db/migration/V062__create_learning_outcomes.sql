CREATE TABLE gts_learning_outcome (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    curriculum_version_id UUID NOT NULL
        REFERENCES gts_curriculum_version(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    outcome_code VARCHAR(100) NOT NULL,

    outcome_title VARCHAR(300) NOT NULL,

    description TEXT,

    outcome_type VARCHAR(50),

    competency_area VARCHAR(200),

    sequence_number INTEGER NOT NULL,

    effective_from DATE,

    effective_to DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_gts_learning_outcome_code
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            outcome_code
        ),


    CONSTRAINT ck_gts_learning_outcome_sequence
        CHECK (
            sequence_number > 0
        ),


    CONSTRAINT ck_gts_learning_outcome_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),


    CONSTRAINT ck_gts_learning_outcome_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);
