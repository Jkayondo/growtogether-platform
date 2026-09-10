-- GT-GRADING-AGGREGATION-001
--
-- Configurable aggregation rules.
-- Supports:
-- Uganda PLE aggregates
-- UACE points
-- CBC competency summaries
-- Institution-defined calculations.
--
-- Aggregation logic is configuration driven.


CREATE TABLE gts_grade_aggregation_rule (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),


    grading_scheme_id UUID NOT NULL
        REFERENCES gts_grading_scheme(id)
        ON DELETE CASCADE,


    rule_code VARCHAR(100) NOT NULL,

    rule_name VARCHAR(250) NOT NULL,


    aggregation_type VARCHAR(80) NOT NULL,


    required_subject_count INTEGER,


    best_subject_count INTEGER,


    include_compulsory_subjects BOOLEAN NOT NULL DEFAULT FALSE,


    uses_grade_points BOOLEAN NOT NULL DEFAULT FALSE,


    description VARCHAR(1500),


    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_grade_aggregation_rule_code
        UNIQUE (
            tenant_id,
            grading_scheme_id,
            rule_code
        ),


    CONSTRAINT ck_grade_aggregation_type
        CHECK (
            aggregation_type IN (
                'SUBJECT_POINT_SUM',
                'PRINCIPAL_SUBSIDIARY_POINTS',
                'COMPETENCY_SUMMARY',
                'AVERAGE_SCORE',
                'CUSTOM'
            )
        ),


    CONSTRAINT ck_grade_aggregation_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);


CREATE INDEX ix_grade_aggregation_scheme
    ON gts_grade_aggregation_rule(
        tenant_id,
        grading_scheme_id
    );

