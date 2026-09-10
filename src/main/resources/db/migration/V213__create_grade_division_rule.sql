-- GT-GRADING-DIVISION-001
--
-- Configurable division classification rules.
-- Supports:
-- Uganda PLE divisions
-- UCE divisions
-- Institution-defined classifications.
--
-- Division logic is configuration driven.
-- No division calculation is hard-coded.


CREATE TABLE gts_grade_division_rule (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),


    grading_scheme_id UUID NOT NULL
        REFERENCES gts_grading_scheme(id)
        ON DELETE CASCADE,


    division_code VARCHAR(100) NOT NULL,

    division_name VARCHAR(250) NOT NULL,


    minimum_aggregate INTEGER NOT NULL,

    maximum_aggregate INTEGER NOT NULL,


    grade_point NUMERIC(8,2),


    description VARCHAR(1500),


    sequence_number INTEGER NOT NULL,


    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_grade_division_rule_code
        UNIQUE (
            tenant_id,
            grading_scheme_id,
            division_code
        ),


    CONSTRAINT uq_grade_division_rule_sequence
        UNIQUE (
            tenant_id,
            grading_scheme_id,
            sequence_number
        ),


    CONSTRAINT ck_grade_division_rule_range
        CHECK (
            maximum_aggregate >= minimum_aggregate
        ),


    CONSTRAINT ck_grade_division_rule_sequence
        CHECK (
            sequence_number > 0
        ),


    CONSTRAINT ck_grade_division_rule_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )

);


CREATE INDEX ix_grade_division_rule_scheme
    ON gts_grade_division_rule(
        tenant_id,
        grading_scheme_id
    );

