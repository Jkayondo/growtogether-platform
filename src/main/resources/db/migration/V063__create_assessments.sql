CREATE TABLE gts_assessment (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learning_outcome_id UUID NOT NULL
        REFERENCES gts_learning_outcome(id),

    assessment_code VARCHAR(100) NOT NULL,

    assessment_title VARCHAR(300) NOT NULL,

    assessment_type VARCHAR(50),

    description TEXT,

    assessment_method VARCHAR(100),

    maximum_score DOUBLE PRECISION,

    passing_score DOUBLE PRECISION,

    weight_percentage DOUBLE PRECISION,

    assessment_date DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_gts_assessment_code

        UNIQUE (
            tenant_id,
            learning_outcome_id,
            assessment_code
        ),


    CONSTRAINT ck_gts_assessment_scores

        CHECK (
            maximum_score IS NULL
            OR maximum_score >= 0
        ),


    CONSTRAINT ck_gts_assessment_passing_score

        CHECK (
            passing_score IS NULL
            OR passing_score >= 0
        ),


    CONSTRAINT ck_gts_assessment_weight

        CHECK (
            weight_percentage IS NULL
            OR (
                weight_percentage >= 0
                AND weight_percentage <= 100
            )
        ),


    CONSTRAINT ck_gts_assessment_status

        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);
