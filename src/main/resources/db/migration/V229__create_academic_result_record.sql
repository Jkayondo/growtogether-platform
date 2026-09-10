-- GT-ACADEMIC-RESULT-001
--
-- Authoritative learner academic result record.
--
-- Stores calculated academic outcomes from the grading engine.
--
-- Used by:
-- Report Cards
-- Transcripts
-- Result Publication
-- Academic Analytics
--
-- No grading logic is stored here.
-- The grading engine remains the calculation authority.


CREATE TABLE academic_result_record (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),


    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),


    student_id UUID NOT NULL
        REFERENCES gts_student(id),


    academic_year_id UUID,

    term_id UUID,


    grading_profile_id UUID,

    grading_scheme_id UUID,


    subject_id UUID,


    subject_name VARCHAR(200) NOT NULL,


    assessment_score NUMERIC(10,2),


    grade_code VARCHAR(40),

    grade_name VARCHAR(120),

    grade_point NUMERIC(10,2),


    aggregate_value NUMERIC(10,2),


    division_code VARCHAR(50),

    division_name VARCHAR(120),


    status VARCHAR(30) NOT NULL DEFAULT 'CALCULATED',


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,


    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT ck_academic_result_record_status

        CHECK (

            status IN (

                'DRAFT',

                'CALCULATED',

                'VERIFIED',

                'PUBLISHED',

                'ARCHIVED'

            )

        )

);



CREATE INDEX ix_academic_result_record_tenant

ON academic_result_record(

    tenant_id

);



CREATE INDEX ix_academic_result_record_student

ON academic_result_record(

    student_id

);



CREATE INDEX ix_academic_result_record_scheme

ON academic_result_record(

    grading_scheme_id

);

