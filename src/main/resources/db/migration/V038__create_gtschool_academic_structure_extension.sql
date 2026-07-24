CREATE TABLE gts_academic_department (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    department_code VARCHAR(80) NOT NULL,
    department_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    parent_department_id UUID
        REFERENCES gts_academic_department(id),

    ewf_organizational_unit_id UUID
        REFERENCES ewf_organizational_unit(id),

    head_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    effective_from DATE,
    effective_to DATE,

    department_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_department_code
        UNIQUE (tenant_id, department_code),

    CONSTRAINT ck_gts_academic_department_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_academic_department_lifecycle
        CHECK (
            department_status IN (
                'PLANNED',
                'ACTIVE',
                'SUSPENDED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_academic_department_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_academic_programme (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    programme_code VARCHAR(80) NOT NULL,
    programme_name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    description VARCHAR(1500),

    academic_department_id UUID
        REFERENCES gts_academic_department(id),

    education_level_id UUID NOT NULL
        REFERENCES gts_education_level(id),

    programme_type VARCHAR(40) NOT NULL,
    delivery_mode VARCHAR(30) NOT NULL DEFAULT 'FACE_TO_FACE',

    minimum_duration_terms INTEGER,
    maximum_duration_terms INTEGER,

    qualification_awarded VARCHAR(250),
    external_authority VARCHAR(250),
    external_programme_code VARCHAR(120),

    effective_from DATE,
    effective_to DATE,

    programme_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_programme_code
        UNIQUE (tenant_id, programme_code),

    CONSTRAINT ck_gts_academic_programme_type
        CHECK (
            programme_type IN (
                'EARLY_CHILDHOOD',
                'PRIMARY',
                'LOWER_SECONDARY',
                'UPPER_SECONDARY',
                'ADVANCED_LEVEL',
                'VOCATIONAL',
                'TECHNICAL',
                'SPECIAL_NEEDS',
                'SHORT_COURSE',
                'BRIDGING',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_academic_delivery_mode
        CHECK (
            delivery_mode IN (
                'FACE_TO_FACE',
                'ONLINE',
                'BLENDED',
                'DISTANCE',
                'WORK_BASED',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_academic_programme_duration
        CHECK (
            (
                minimum_duration_terms IS NULL
                OR minimum_duration_terms > 0
            )
            AND (
                maximum_duration_terms IS NULL
                OR maximum_duration_terms > 0
            )
            AND (
                minimum_duration_terms IS NULL
                OR maximum_duration_terms IS NULL
                OR maximum_duration_terms >= minimum_duration_terms
            )
        ),

    CONSTRAINT ck_gts_academic_programme_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_academic_programme_lifecycle
        CHECK (
            programme_status IN (
                'DRAFT',
                'ACTIVE',
                'SUSPENDED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_academic_programme_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_study_track (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    academic_programme_id UUID NOT NULL
        REFERENCES gts_academic_programme(id),

    track_code VARCHAR(80) NOT NULL,
    track_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    specialization_area VARCHAR(200),

    effective_from DATE,
    effective_to DATE,

    track_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_study_track_code
        UNIQUE (tenant_id, academic_programme_id, track_code),

    CONSTRAINT ck_gts_study_track_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_study_track_lifecycle
        CHECK (
            track_status IN (
                'DRAFT',
                'ACTIVE',
                'SUSPENDED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_study_track_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_curriculum (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    curriculum_code VARCHAR(100) NOT NULL,
    curriculum_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    curriculum_authority VARCHAR(250),
    country_code VARCHAR(3),
    education_system VARCHAR(120),

    curriculum_type VARCHAR(40) NOT NULL DEFAULT 'NATIONAL',
    approval_reference VARCHAR(160),

    effective_from DATE,
    effective_to DATE,

    curriculum_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_curriculum_code
        UNIQUE (tenant_id, curriculum_code),

    CONSTRAINT ck_gts_curriculum_type
        CHECK (
            curriculum_type IN (
                'NATIONAL',
                'INTERNATIONAL',
                'INSTITUTIONAL',
                'VOCATIONAL',
                'RELIGIOUS',
                'SPECIAL_NEEDS',
                'BLENDED',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_curriculum_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_curriculum_lifecycle
        CHECK (
            curriculum_status IN (
                'DRAFT',
                'APPROVED',
                'ACTIVE',
                'SUPERSEDED',
                'RETIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_curriculum_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_curriculum_version (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    curriculum_id UUID NOT NULL
        REFERENCES gts_curriculum(id),

    version_code VARCHAR(80) NOT NULL,
    version_name VARCHAR(200),

    publication_date DATE,
    effective_from DATE NOT NULL,
    effective_to DATE,

    change_summary VARCHAR(2000),
    approval_reference VARCHAR(160),

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    version_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_curriculum_version_code
        UNIQUE (tenant_id, curriculum_id, version_code),

    CONSTRAINT ck_gts_curriculum_version_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_curriculum_version_approval
        CHECK (
            version_status NOT IN ('APPROVED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_curriculum_version_lifecycle
        CHECK (
            version_status IN (
                'DRAFT',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'SUPERSEDED',
                'RETIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_curriculum_version_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_active_curriculum_version
    ON gts_curriculum_version (tenant_id, curriculum_id)
    WHERE version_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE gts_curriculum_class_grade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    curriculum_version_id UUID NOT NULL
        REFERENCES gts_curriculum_version(id) ON DELETE CASCADE,

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    study_track_id UUID
        REFERENCES gts_study_track(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    sequence_number INTEGER NOT NULL,
    minimum_age INTEGER,
    maximum_age INTEGER,

    mandatory_stage BOOLEAN NOT NULL DEFAULT TRUE,

    effective_from DATE,
    effective_to DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_curriculum_class_grade
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            academic_programme_id,
            study_track_id,
            class_grade_id
        ),

    CONSTRAINT uq_gts_curriculum_class_sequence
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            academic_programme_id,
            study_track_id,
            sequence_number
        ),

    CONSTRAINT ck_gts_curriculum_class_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_curriculum_class_ages
        CHECK (
            (
                minimum_age IS NULL
                OR minimum_age >= 0
            )
            AND (
                maximum_age IS NULL
                OR maximum_age >= 0
            )
            AND (
                minimum_age IS NULL
                OR maximum_age IS NULL
                OR maximum_age >= minimum_age
            )
        ),

    CONSTRAINT ck_gts_curriculum_class_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_curriculum_class_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_curriculum_subject (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    curriculum_version_id UUID NOT NULL
        REFERENCES gts_curriculum_version(id) ON DELETE CASCADE,

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    study_track_id UUID
        REFERENCES gts_study_track(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    subject_requirement VARCHAR(30) NOT NULL DEFAULT 'CORE',

    minimum_weekly_periods INTEGER,
    maximum_weekly_periods INTEGER,
    recommended_weekly_periods INTEGER,

    credit_value NUMERIC(8,2),
    pass_mark NUMERIC(6,2),

    effective_from DATE,
    effective_to DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_curriculum_subject
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            academic_programme_id,
            study_track_id,
            class_grade_id,
            subject_id
        ),

    CONSTRAINT ck_gts_curriculum_subject_requirement
        CHECK (
            subject_requirement IN (
                'CORE',
                'COMPULSORY',
                'ELECTIVE',
                'OPTIONAL',
                'VOCATIONAL',
                'CO_CURRICULAR'
            )
        ),

    CONSTRAINT ck_gts_curriculum_subject_periods
        CHECK (
            (
                minimum_weekly_periods IS NULL
                OR minimum_weekly_periods >= 0
            )
            AND (
                maximum_weekly_periods IS NULL
                OR maximum_weekly_periods >= 0
            )
            AND (
                recommended_weekly_periods IS NULL
                OR recommended_weekly_periods >= 0
            )
            AND (
                minimum_weekly_periods IS NULL
                OR maximum_weekly_periods IS NULL
                OR maximum_weekly_periods >= minimum_weekly_periods
            )
        ),

    CONSTRAINT ck_gts_curriculum_subject_credit
        CHECK (
            credit_value IS NULL
            OR credit_value >= 0
        ),

    CONSTRAINT ck_gts_curriculum_subject_pass_mark
        CHECK (
            pass_mark IS NULL
            OR (
                pass_mark >= 0
                AND pass_mark <= 100
            )
        ),

    CONSTRAINT ck_gts_curriculum_subject_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_curriculum_subject_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_subject_prerequisite (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    curriculum_version_id UUID
        REFERENCES gts_curriculum_version(id),

    subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    prerequisite_subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    prerequisite_type VARCHAR(30) NOT NULL DEFAULT 'REQUIRED',
    minimum_grade_code VARCHAR(40),
    minimum_score NUMERIC(6,2),

    notes VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_subject_prerequisite
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            subject_id,
            prerequisite_subject_id
        ),

    CONSTRAINT ck_gts_subject_not_own_prerequisite
        CHECK (subject_id <> prerequisite_subject_id),

    CONSTRAINT ck_gts_subject_prerequisite_type
        CHECK (
            prerequisite_type IN (
                'REQUIRED',
                'RECOMMENDED',
                'CO_REQUISITE',
                'ALTERNATIVE'
            )
        ),

    CONSTRAINT ck_gts_subject_prerequisite_score
        CHECK (
            minimum_score IS NULL
            OR (
                minimum_score >= 0
                AND minimum_score <= 100
            )
        ),

    CONSTRAINT ck_gts_subject_prerequisite_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_class_offering (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    offering_code VARCHAR(100) NOT NULL,

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    study_track_id UUID
        REFERENCES gts_study_track(id),

    curriculum_version_id UUID
        REFERENCES gts_curriculum_version(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    planned_capacity INTEGER,
    minimum_enrollment INTEGER,
    maximum_enrollment INTEGER,

    enrollment_open_date DATE,
    enrollment_close_date DATE,

    offering_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_class_offering_code
        UNIQUE (tenant_id, offering_code),

    CONSTRAINT uq_gts_class_offering_scope
        UNIQUE (
            tenant_id,
            academic_year_id,
            campus_id,
            academic_programme_id,
            study_track_id,
            class_grade_id
        ),

    CONSTRAINT ck_gts_class_offering_capacity
        CHECK (
            (
                planned_capacity IS NULL
                OR planned_capacity > 0
            )
            AND (
                minimum_enrollment IS NULL
                OR minimum_enrollment >= 0
            )
            AND (
                maximum_enrollment IS NULL
                OR maximum_enrollment > 0
            )
            AND (
                minimum_enrollment IS NULL
                OR maximum_enrollment IS NULL
                OR maximum_enrollment >= minimum_enrollment
            )
        ),

    CONSTRAINT ck_gts_class_offering_enrollment_dates
        CHECK (
            enrollment_close_date IS NULL
            OR enrollment_open_date IS NULL
            OR enrollment_close_date >= enrollment_open_date
        ),

    CONSTRAINT ck_gts_class_offering_lifecycle
        CHECK (
            offering_status IN (
                'PLANNED',
                'OPEN',
                'ACTIVE',
                'CLOSED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_class_offering_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_subject_offering (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    subject_offering_code VARCHAR(120) NOT NULL,

    class_offering_id UUID NOT NULL
        REFERENCES gts_class_offering(id) ON DELETE CASCADE,

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    academic_department_id UUID
        REFERENCES gts_academic_department(id),

    grading_scheme_id UUID,

    weekly_periods INTEGER,
    credit_value NUMERIC(8,2),

    minimum_enrollment INTEGER,
    maximum_enrollment INTEGER,

    offering_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_subject_offering_code
        UNIQUE (tenant_id, subject_offering_code),

    CONSTRAINT uq_gts_subject_offering_scope
        UNIQUE (
            tenant_id,
            class_offering_id,
            academic_term_id,
            stream_id,
            subject_id
        ),

    CONSTRAINT ck_gts_subject_offering_periods
        CHECK (
            weekly_periods IS NULL
            OR weekly_periods > 0
        ),

    CONSTRAINT ck_gts_subject_offering_credit
        CHECK (
            credit_value IS NULL
            OR credit_value >= 0
        ),

    CONSTRAINT ck_gts_subject_offering_enrollment
        CHECK (
            (
                minimum_enrollment IS NULL
                OR minimum_enrollment >= 0
            )
            AND (
                maximum_enrollment IS NULL
                OR maximum_enrollment > 0
            )
            AND (
                minimum_enrollment IS NULL
                OR maximum_enrollment IS NULL
                OR maximum_enrollment >= minimum_enrollment
            )
        ),

    CONSTRAINT ck_gts_subject_offering_lifecycle
        CHECK (
            offering_status IN (
                'PLANNED',
                'OPEN',
                'ACTIVE',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_subject_offering_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_grading_scheme (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    scheme_code VARCHAR(80) NOT NULL,
    scheme_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    scheme_type VARCHAR(30) NOT NULL DEFAULT 'PERCENTAGE',
    minimum_score NUMERIC(8,2) NOT NULL DEFAULT 0,
    maximum_score NUMERIC(8,2) NOT NULL DEFAULT 100,
    pass_score NUMERIC(8,2),

    decimal_places INTEGER NOT NULL DEFAULT 2,
    rounding_method VARCHAR(30) NOT NULL DEFAULT 'HALF_UP',

    effective_from DATE,
    effective_to DATE,

    scheme_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_grading_scheme_code
        UNIQUE (tenant_id, scheme_code),

    CONSTRAINT ck_gts_grading_scheme_type
        CHECK (
            scheme_type IN (
                'PERCENTAGE',
                'LETTER_GRADE',
                'POINTS',
                'COMPETENCY',
                'PASS_FAIL',
                'DESCRIPTIVE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_grading_scheme_range
        CHECK (
            maximum_score > minimum_score
            AND (
                pass_score IS NULL
                OR (
                    pass_score >= minimum_score
                    AND pass_score <= maximum_score
                )
            )
        ),

    CONSTRAINT ck_gts_grading_scheme_decimals
        CHECK (
            decimal_places >= 0
            AND decimal_places <= 6
        ),

    CONSTRAINT ck_gts_grading_rounding_method
        CHECK (
            rounding_method IN (
                'HALF_UP',
                'HALF_DOWN',
                'HALF_EVEN',
                'UP',
                'DOWN',
                'CEILING',
                'FLOOR'
            )
        ),

    CONSTRAINT ck_gts_grading_scheme_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_grading_scheme_lifecycle
        CHECK (
            scheme_status IN (
                'DRAFT',
                'ACTIVE',
                'INACTIVE',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_grading_scheme_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_grading_scheme
    FOREIGN KEY (grading_scheme_id)
    REFERENCES gts_grading_scheme(id);

CREATE TABLE gts_grade_boundary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    grading_scheme_id UUID NOT NULL
        REFERENCES gts_grading_scheme(id) ON DELETE CASCADE,

    grade_code VARCHAR(40) NOT NULL,
    grade_name VARCHAR(120) NOT NULL,

    minimum_score NUMERIC(8,2) NOT NULL,
    maximum_score NUMERIC(8,2) NOT NULL,

    grade_point NUMERIC(8,2),
    pass_grade BOOLEAN NOT NULL DEFAULT TRUE,
    distinction_grade BOOLEAN NOT NULL DEFAULT FALSE,

    description VARCHAR(500),
    sequence_number INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_grade_boundary_code
        UNIQUE (tenant_id, grading_scheme_id, grade_code),

    CONSTRAINT uq_gts_grade_boundary_sequence
        UNIQUE (tenant_id, grading_scheme_id, sequence_number),

    CONSTRAINT ck_gts_grade_boundary_range
        CHECK (maximum_score >= minimum_score),

    CONSTRAINT ck_gts_grade_boundary_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_grade_boundary_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_academic_calendar_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID
        REFERENCES gts_campus(id),

    event_code VARCHAR(100) NOT NULL,
    event_name VARCHAR(250) NOT NULL,
    event_description VARCHAR(1500),

    event_type VARCHAR(40) NOT NULL,

    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ,

    all_day BOOLEAN NOT NULL DEFAULT FALSE,
    instructional_day BOOLEAN NOT NULL DEFAULT FALSE,
    institution_closed BOOLEAN NOT NULL DEFAULT FALSE,

    recurrence_rule VARCHAR(500),
    notification_required BOOLEAN NOT NULL DEFAULT FALSE,

    eds_document_id UUID,
    workflow_instance_id UUID,

    event_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_calendar_event_code
        UNIQUE (tenant_id, academic_year_id, event_code),

    CONSTRAINT ck_gts_academic_calendar_event_type
        CHECK (
            event_type IN (
                'TERM_START',
                'TERM_END',
                'TEACHING_DAY',
                'HOLIDAY',
                'EXAMINATION',
                'ASSESSMENT',
                'REGISTRATION',
                'ADMISSION',
                'REPORTING_DAY',
                'PARENT_MEETING',
                'STAFF_MEETING',
                'SPORTS',
                'FIELD_TRIP',
                'GRADUATION',
                'RELIGIOUS_EVENT',
                'MAINTENANCE',
                'EMERGENCY_CLOSURE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_academic_calendar_event_dates
        CHECK (
            end_at IS NULL
            OR end_at >= start_at
        ),

    CONSTRAINT ck_gts_academic_calendar_event_lifecycle
        CHECK (
            event_status IN (
                'DRAFT',
                'SCHEDULED',
                'CONFIRMED',
                'IN_PROGRESS',
                'COMPLETED',
                'CANCELLED',
                'POSTPONED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_academic_calendar_event_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_progression_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    rule_code VARCHAR(100) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    description VARCHAR(1500),

    curriculum_version_id UUID
        REFERENCES gts_curriculum_version(id),

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    study_track_id UUID
        REFERENCES gts_study_track(id),

    from_class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    to_class_grade_id UUID
        REFERENCES gts_class_grade(id),

    rule_type VARCHAR(40) NOT NULL DEFAULT 'PROMOTION',

    minimum_average_score NUMERIC(6,2),
    minimum_subjects_passed INTEGER,
    maximum_failed_subjects INTEGER,
    minimum_attendance_percentage NUMERIC(6,2),

    automatic_progression BOOLEAN NOT NULL DEFAULT FALSE,
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,

    criteria JSONB NOT NULL DEFAULT '{}'::jsonb,

    effective_from DATE,
    effective_to DATE,

    workflow_definition_reference UUID,
    rule_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_progression_rule_code
        UNIQUE (tenant_id, rule_code),

    CONSTRAINT ck_gts_progression_rule_type
        CHECK (
            rule_type IN (
                'PROMOTION',
                'PROGRESSION',
                'REPEAT',
                'GRADUATION',
                'TRANSFER',
                'COMPLETION',
                'PROBATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_progression_rule_scores
        CHECK (
            (
                minimum_average_score IS NULL
                OR (
                    minimum_average_score >= 0
                    AND minimum_average_score <= 100
                )
            )
            AND (
                minimum_attendance_percentage IS NULL
                OR (
                    minimum_attendance_percentage >= 0
                    AND minimum_attendance_percentage <= 100
                )
            )
            AND (
                minimum_subjects_passed IS NULL
                OR minimum_subjects_passed >= 0
            )
            AND (
                maximum_failed_subjects IS NULL
                OR maximum_failed_subjects >= 0
            )
        ),

    CONSTRAINT ck_gts_progression_rule_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_progression_rule_lifecycle
        CHECK (
            rule_status IN (
                'DRAFT',
                'ACTIVE',
                'SUSPENDED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_progression_rule_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE INDEX ix_gts_academic_department_parent
    ON gts_academic_department (
        tenant_id,
        parent_department_id,
        department_status
    );

CREATE INDEX ix_gts_academic_programme_level
    ON gts_academic_programme (
        tenant_id,
        education_level_id,
        programme_status
    );

CREATE INDEX ix_gts_study_track_programme
    ON gts_study_track (
        tenant_id,
        academic_programme_id,
        track_status
    );

CREATE INDEX ix_gts_curriculum_country
    ON gts_curriculum (
        tenant_id,
        country_code,
        curriculum_status
    );

CREATE INDEX ix_gts_curriculum_version_effective
    ON gts_curriculum_version (
        tenant_id,
        curriculum_id,
        effective_from,
        version_status
    );

CREATE INDEX ix_gts_curriculum_class_grade
    ON gts_curriculum_class_grade (
        tenant_id,
        curriculum_version_id,
        class_grade_id
    );

CREATE INDEX ix_gts_curriculum_subject_class
    ON gts_curriculum_subject (
        tenant_id,
        curriculum_version_id,
        class_grade_id,
        subject_requirement
    );

CREATE INDEX ix_gts_subject_prerequisite_subject
    ON gts_subject_prerequisite (
        tenant_id,
        subject_id,
        prerequisite_type
    );

CREATE INDEX ix_gts_class_offering_year
    ON gts_class_offering (
        tenant_id,
        academic_year_id,
        campus_id,
        offering_status
    );

CREATE INDEX ix_gts_subject_offering_class
    ON gts_subject_offering (
        tenant_id,
        class_offering_id,
        academic_term_id,
        offering_status
    );

CREATE INDEX ix_gts_grading_scheme_type
    ON gts_grading_scheme (
        tenant_id,
        scheme_type,
        scheme_status
    );

CREATE INDEX ix_gts_grade_boundary_scheme
    ON gts_grade_boundary (
        tenant_id,
        grading_scheme_id,
        sequence_number
    );

CREATE INDEX ix_gts_academic_calendar_period
    ON gts_academic_calendar_event (
        tenant_id,
        academic_year_id,
        academic_term_id,
        start_at
    );

CREATE INDEX ix_gts_progression_rule_class
    ON gts_progression_rule (
        tenant_id,
        from_class_grade_id,
        to_class_grade_id,
        rule_status
    );

UPDATE platform_metadata
SET metadata_value = '038',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
