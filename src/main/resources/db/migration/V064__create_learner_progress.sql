CREATE TABLE gts_learner_progress (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    assessment_id UUID NOT NULL
        REFERENCES gts_assessment(id),

    learning_outcome_id UUID NOT NULL
        REFERENCES gts_learning_outcome(id),

    score DOUBLE PRECISION,

    maximum_score DOUBLE PRECISION,

    percentage_score DOUBLE PRECISION,

    achievement_status VARCHAR(50),

    teacher_comment TEXT,

    assessment_date DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT ck_gts_learner_progress_scores

    CHECK (
        score IS NULL
        OR score >= 0
    ),


    CONSTRAINT ck_gts_learner_progress_max_score

    CHECK (
        maximum_score IS NULL
        OR maximum_score >= 0
    ),


    CONSTRAINT ck_gts_learner_progress_percentage

    CHECK (
        percentage_score IS NULL
        OR (
            percentage_score >= 0
            AND percentage_score <= 100
        )
    ),


    CONSTRAINT ck_gts_learner_progress_status

    CHECK (
        status IN (
            'ACTIVE',
            'INACTIVE',
            'ARCHIVED'
        )
    )

);
