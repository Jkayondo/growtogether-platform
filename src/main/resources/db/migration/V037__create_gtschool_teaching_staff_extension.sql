CREATE TABLE gts_teacher_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id),

    teacher_number VARCHAR(80) NOT NULL,
    teacher_registration_number VARCHAR(120),
    teaching_licence_number VARCHAR(120),
    teaching_licence_issued_at DATE,
    teaching_licence_expires_at DATE,

    highest_teaching_level VARCHAR(80),
    primary_specialization VARCHAR(200),
    secondary_specialization VARCHAR(200),

    teacher_category VARCHAR(40) NOT NULL DEFAULT 'CLASSROOM_TEACHER',
    teaching_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    qualified_for_boarding_duty BOOLEAN NOT NULL DEFAULT FALSE,
    qualified_for_special_needs BOOLEAN NOT NULL DEFAULT FALSE,
    qualified_for_counselling BOOLEAN NOT NULL DEFAULT FALSE,

    maximum_weekly_periods INTEGER,
    notes VARCHAR(1500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_teacher_workforce_member
        UNIQUE (tenant_id, workforce_member_id),

    CONSTRAINT uq_gts_teacher_number
        UNIQUE (tenant_id, teacher_number),

    CONSTRAINT uq_gts_teacher_registration
        UNIQUE (tenant_id, teacher_registration_number),

    CONSTRAINT ck_gts_teacher_licence_dates
        CHECK (
            teaching_licence_expires_at IS NULL
            OR teaching_licence_issued_at IS NULL
            OR teaching_licence_expires_at >= teaching_licence_issued_at
        ),

    CONSTRAINT ck_gts_teacher_category
        CHECK (
            teacher_category IN (
                'CLASSROOM_TEACHER',
                'SUBJECT_TEACHER',
                'VOCATIONAL_INSTRUCTOR',
                'SPECIAL_NEEDS_TEACHER',
                'COUNSELLOR',
                'HEAD_OF_DEPARTMENT',
                'DIRECTOR_OF_STUDIES',
                'DEPUTY_HEADTEACHER',
                'HEADTEACHER',
                'ACADEMIC_ADMINISTRATOR',
                'TEACHING_ASSISTANT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_teacher_lifecycle
        CHECK (
            teaching_status IN (
                'PENDING',
                'ACTIVE',
                'ON_LEAVE',
                'SUSPENDED',
                'TRANSFERRED',
                'INACTIVE',
                'RETIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_teacher_maximum_periods
        CHECK (
            maximum_weekly_periods IS NULL
            OR maximum_weekly_periods > 0
        ),

    CONSTRAINT ck_gts_teacher_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_teacher_subject_qualification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id) ON DELETE CASCADE,

    subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    competency_level VARCHAR(30) NOT NULL DEFAULT 'QUALIFIED',
    primary_subject BOOLEAN NOT NULL DEFAULT FALSE,

    minimum_class_grade_id UUID
        REFERENCES gts_class_grade(id),

    maximum_class_grade_id UUID
        REFERENCES gts_class_grade(id),

    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    eds_evidence_document_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_teacher_subject_qualification
        UNIQUE (tenant_id, teacher_profile_id, subject_id),

    CONSTRAINT ck_gts_teacher_subject_competency
        CHECK (
            competency_level IN (
                'TRAINEE',
                'ASSISTANT',
                'QUALIFIED',
                'ADVANCED',
                'SPECIALIST'
            )
        ),

    CONSTRAINT ck_gts_teacher_subject_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_teacher_subject_verification
        CHECK (
            verification_status IN (
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'EXPIRED'
            )
        ),

    CONSTRAINT ck_gts_teacher_subject_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_teacher_subject_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_teacher_primary_subject
    ON gts_teacher_subject_qualification (
        tenant_id,
        teacher_profile_id
    )
    WHERE primary_subject = TRUE
      AND status = 'ACTIVE';

CREATE TABLE gts_teaching_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    assignment_reference VARCHAR(100) NOT NULL,

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id),

    ewf_assignment_id UUID
        REFERENCES ewf_assignment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    subject_id UUID NOT NULL
        REFERENCES gts_subject(id),

    assignment_type VARCHAR(30) NOT NULL DEFAULT 'PRIMARY_TEACHER',
    weekly_periods INTEGER NOT NULL,
    workload_percentage NUMERIC(5,2),

    effective_from DATE NOT NULL,
    effective_to DATE,

    room_reference VARCHAR(120),
    timetable_reference UUID,

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_teaching_assignment_reference
        UNIQUE (tenant_id, assignment_reference),

    CONSTRAINT uq_gts_teaching_assignment_scope
        UNIQUE (
            tenant_id,
            teacher_profile_id,
            academic_year_id,
            academic_term_id,
            class_grade_id,
            stream_id,
            subject_id
        ),

    CONSTRAINT ck_gts_teaching_assignment_type
        CHECK (
            assignment_type IN (
                'PRIMARY_TEACHER',
                'ASSISTANT_TEACHER',
                'RELIEF_TEACHER',
                'PRACTICAL_INSTRUCTOR',
                'SPECIAL_NEEDS_SUPPORT',
                'REMOTE_TEACHER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_teaching_assignment_periods
        CHECK (weekly_periods > 0),

    CONSTRAINT ck_gts_teaching_assignment_workload
        CHECK (
            workload_percentage IS NULL
            OR (
                workload_percentage > 0
                AND workload_percentage <= 100
            )
        ),

    CONSTRAINT ck_gts_teaching_assignment_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_teaching_assignment_lifecycle
        CHECK (
            assignment_status IN (
                'PLANNED',
                'PENDING_APPROVAL',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_teaching_assignment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_class_teacher_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    responsibility_type VARCHAR(30) NOT NULL DEFAULT 'CLASS_TEACHER',

    effective_from DATE NOT NULL,
    effective_to DATE,

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_class_teacher_responsibility
        CHECK (
            responsibility_type IN (
                'CLASS_TEACHER',
                'ASSISTANT_CLASS_TEACHER',
                'YEAR_HEAD',
                'STREAM_COORDINATOR'
            )
        ),

    CONSTRAINT ck_gts_class_teacher_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_class_teacher_lifecycle
        CHECK (
            assignment_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_class_teacher_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_active_class_teacher
    ON gts_class_teacher_assignment (
        tenant_id,
        academic_year_id,
        academic_term_id,
        campus_id,
        class_grade_id,
        stream_id,
        responsibility_type
    )
    WHERE assignment_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE gts_teacher_workload (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    planned_teaching_periods INTEGER NOT NULL DEFAULT 0,
    actual_teaching_periods INTEGER NOT NULL DEFAULT 0,
    extracurricular_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    administrative_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    examination_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    meeting_hours NUMERIC(6,2) NOT NULL DEFAULT 0,
    counselling_hours NUMERIC(6,2) NOT NULL DEFAULT 0,

    maximum_allowed_periods INTEGER,
    overload_periods INTEGER NOT NULL DEFAULT 0,

    calculated_at TIMESTAMPTZ,
    calculated_by UUID,

    workload_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_teacher_workload_period
        UNIQUE (
            tenant_id,
            teacher_profile_id,
            academic_year_id,
            academic_term_id
        ),

    CONSTRAINT ck_gts_teacher_workload_values
        CHECK (
            planned_teaching_periods >= 0
            AND actual_teaching_periods >= 0
            AND extracurricular_hours >= 0
            AND administrative_hours >= 0
            AND examination_hours >= 0
            AND meeting_hours >= 0
            AND counselling_hours >= 0
            AND overload_periods >= 0
        ),

    CONSTRAINT ck_gts_teacher_workload_maximum
        CHECK (
            maximum_allowed_periods IS NULL
            OR maximum_allowed_periods > 0
        ),

    CONSTRAINT ck_gts_teacher_workload_lifecycle
        CHECK (
            workload_status IN (
                'DRAFT',
                'CALCULATED',
                'REVIEWED',
                'APPROVED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_teacher_workload_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_teacher_duty_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    duty_type VARCHAR(40) NOT NULL,
    duty_description VARCHAR(1000),

    duty_date DATE,
    day_of_week VARCHAR(15),
    start_time TIME,
    end_time TIME,
    location_reference VARCHAR(160),

    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_rule VARCHAR(500),

    workflow_instance_id UUID,
    duty_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_teacher_duty_type
        CHECK (
            duty_type IN (
                'MORNING_DUTY',
                'EVENING_DUTY',
                'ASSEMBLY',
                'DINING_HALL',
                'BOARDING',
                'SPORTS',
                'WEEKEND_SUPERVISION',
                'EXAMINATION',
                'STUDY_SUPERVISION',
                'TRANSPORT',
                'EVENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_teacher_duty_day
        CHECK (
            day_of_week IS NULL
            OR day_of_week IN (
                'MONDAY',
                'TUESDAY',
                'WEDNESDAY',
                'THURSDAY',
                'FRIDAY',
                'SATURDAY',
                'SUNDAY'
            )
        ),

    CONSTRAINT ck_gts_teacher_duty_times
        CHECK (
            end_time IS NULL
            OR start_time IS NULL
            OR end_time > start_time
        ),

    CONSTRAINT ck_gts_teacher_duty_lifecycle
        CHECK (
            duty_status IN (
                'SCHEDULED',
                'CONFIRMED',
                'COMPLETED',
                'MISSED',
                'CANCELLED',
                'REASSIGNED'
            )
        ),

    CONSTRAINT ck_gts_teacher_duty_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_examination_staff_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    subject_id UUID
        REFERENCES gts_subject(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    examination_reference UUID,
    assignment_role VARCHAR(30) NOT NULL,

    assignment_date DATE,
    start_time TIME,
    end_time TIME,
    room_reference VARCHAR(120),

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED',
    workflow_instance_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_examination_staff_role
        CHECK (
            assignment_role IN (
                'INVIGILATOR',
                'SUPERVISOR',
                'CHIEF_INVIGILATOR',
                'EXAMINER',
                'MARKER',
                'MODERATOR',
                'PAPER_SETTER',
                'PRACTICAL_ASSESSOR',
                'SPECIAL_NEEDS_SUPPORT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_examination_staff_times
        CHECK (
            end_time IS NULL
            OR start_time IS NULL
            OR end_time > start_time
        ),

    CONSTRAINT ck_gts_examination_staff_lifecycle
        CHECK (
            assignment_status IN (
                'ASSIGNED',
                'CONFIRMED',
                'COMPLETED',
                'DECLINED',
                'CANCELLED',
                'REASSIGNED'
            )
        ),

    CONSTRAINT ck_gts_examination_staff_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_teacher_leave_extension (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id),

    leave_reference VARCHAR(100) NOT NULL,
    leave_type VARCHAR(40) NOT NULL,

    leave_start_date DATE NOT NULL,
    leave_end_date DATE NOT NULL,

    replacement_teacher_profile_id UUID
        REFERENCES gts_teacher_profile(id),

    affected_assignments_reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    timetable_updated BOOLEAN NOT NULL DEFAULT FALSE,

    workflow_instance_id UUID,
    leave_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_teacher_leave_reference
        UNIQUE (tenant_id, leave_reference),

    CONSTRAINT ck_gts_teacher_leave_type
        CHECK (
            leave_type IN (
                'ANNUAL',
                'SICK',
                'MATERNITY',
                'PATERNITY',
                'STUDY',
                'COMPASSIONATE',
                'SABBATICAL',
                'UNPAID',
                'OFFICIAL_DUTY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_teacher_leave_dates
        CHECK (leave_end_date >= leave_start_date),

    CONSTRAINT ck_gts_teacher_leave_lifecycle
        CHECK (
            leave_status IN (
                'DRAFT',
                'PENDING',
                'APPROVED',
                'REJECTED',
                'ACTIVE',
                'COMPLETED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_teacher_leave_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_teacher_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id) ON DELETE CASCADE,

    event_type VARCHAR(50) NOT NULL,
    event_description VARCHAR(1500),

    previous_value JSONB,
    new_value JSONB,

    effective_at TIMESTAMPTZ NOT NULL,
    event_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_teacher_history_event
        CHECK (
            event_type IN (
                'PROFILE_CREATED',
                'LICENCE_UPDATED',
                'SUBJECT_ADDED',
                'SUBJECT_REMOVED',
                'TEACHING_ASSIGNMENT_CREATED',
                'TEACHING_ASSIGNMENT_ENDED',
                'CLASS_TEACHER_ASSIGNED',
                'CLASS_TEACHER_ENDED',
                'WORKLOAD_APPROVED',
                'DUTY_ASSIGNED',
                'EXAMINATION_ASSIGNED',
                'LEAVE_STARTED',
                'LEAVE_ENDED',
                'TRANSFERRED',
                'PROMOTED',
                'SUSPENDED',
                'REINSTATED',
                'RETIRED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_teacher_profile_workforce
    ON gts_teacher_profile (
        tenant_id,
        workforce_member_id,
        teaching_status
    );

CREATE INDEX ix_gts_teacher_subject
    ON gts_teacher_subject_qualification (
        tenant_id,
        subject_id,
        verification_status
    );

CREATE INDEX ix_gts_teaching_assignment_teacher
    ON gts_teaching_assignment (
        tenant_id,
        teacher_profile_id,
        academic_year_id,
        academic_term_id,
        assignment_status
    );

CREATE INDEX ix_gts_teaching_assignment_class
    ON gts_teaching_assignment (
        tenant_id,
        class_grade_id,
        stream_id,
        subject_id,
        assignment_status
    );

CREATE INDEX ix_gts_class_teacher_scope
    ON gts_class_teacher_assignment (
        tenant_id,
        academic_year_id,
        academic_term_id,
        class_grade_id,
        stream_id,
        assignment_status
    );

CREATE INDEX ix_gts_teacher_workload_period
    ON gts_teacher_workload (
        tenant_id,
        academic_year_id,
        academic_term_id,
        workload_status
    );

CREATE INDEX ix_gts_teacher_duty_date
    ON gts_teacher_duty_assignment (
        tenant_id,
        duty_date,
        duty_status
    );

CREATE INDEX ix_gts_examination_staff_teacher
    ON gts_examination_staff_assignment (
        tenant_id,
        teacher_profile_id,
        assignment_date,
        assignment_status
    );

CREATE INDEX ix_gts_teacher_leave_period
    ON gts_teacher_leave_extension (
        tenant_id,
        teacher_profile_id,
        leave_start_date,
        leave_end_date,
        leave_status
    );

CREATE INDEX ix_gts_teacher_history
    ON gts_teacher_history (
        tenant_id,
        teacher_profile_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '037',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
