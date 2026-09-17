-- GT-TEACHER-COVERAGE-001
--
-- Teacher curriculum coverage tracking.
-- Records teacher progress against curriculum expectations.
-- Curriculum remains the source of truth.
-- This table records actual delivery progress.

CREATE TABLE gts_teacher_coverage (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),


    teacher_profile_id UUID NOT NULL,

    teaching_assignment_id UUID NOT NULL,


    academic_year_id UUID NOT NULL,

    academic_term_id UUID,


    curriculum_version_id UUID NOT NULL,

    curriculum_subject_id UUID,

    class_grade_id UUID NOT NULL,


    coverage_type VARCHAR(40) NOT NULL,

    coverage_item VARCHAR(500) NOT NULL,


    planned_week INTEGER,

    coverage_status VARCHAR(40) NOT NULL DEFAULT 'NOT_STARTED',


    completion_date DATE,

    teacher_remarks VARCHAR(1500),


    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',



    CONSTRAINT ck_teacher_coverage_type
        CHECK (
            coverage_type IN (
                'TOPIC',
                'LEARNING_OUTCOME',
                'COMPETENCY',
                'THEME',
                'ACTIVITY'
            )
        ),


    CONSTRAINT ck_teacher_coverage_status
        CHECK (
            coverage_status IN (
                'NOT_STARTED',
                'IN_PROGRESS',
                'COMPLETED',
                'REQUIRES_REMEDIATION',
                'AHEAD_OF_SCHEDULE'
            )
        )

);



CREATE INDEX ix_teacher_coverage_teacher
    ON gts_teacher_coverage(
        tenant_id,
        teacher_profile_id
    );


CREATE INDEX ix_teacher_coverage_assignment
    ON gts_teacher_coverage(
        tenant_id,
        teaching_assignment_id
    );


CREATE INDEX ix_teacher_coverage_curriculum
    ON gts_teacher_coverage(
        tenant_id,
        curriculum_version_id,
        class_grade_id
    );


CREATE INDEX ix_teacher_coverage_term
    ON gts_teacher_coverage(
        tenant_id,
        academic_term_id
    );

