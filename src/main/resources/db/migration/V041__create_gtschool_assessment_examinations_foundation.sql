CREATE TABLE gts_assessment_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    type_code VARCHAR(80) NOT NULL,
    type_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    assessment_category VARCHAR(40) NOT NULL,
    delivery_mode VARCHAR(30) NOT NULL DEFAULT 'IN_PERSON',

    default_weight_percentage NUMERIC(6,2),
    default_maximum_score NUMERIC(10,2),
    default_pass_score NUMERIC(10,2),

    requires_schedule BOOLEAN NOT NULL DEFAULT TRUE,
    requires_candidate_registration BOOLEAN NOT NULL DEFAULT FALSE,
    requires_moderation BOOLEAN NOT NULL DEFAULT FALSE,
    allows_multiple_attempts BOOLEAN NOT NULL DEFAULT FALSE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_assessment_type_code
        UNIQUE (tenant_id, type_code),

    CONSTRAINT ck_gts_assessment_category
        CHECK (
            assessment_category IN (
                'CONTINUOUS_ASSESSMENT',
                'COURSEWORK',
                'HOMEWORK',
                'QUIZ',
                'TEST',
                'PROJECT',
                'PRACTICAL',
                'ORAL',
                'PORTFOLIO',
                'EXAMINATION',
                'COMPETENCY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_assessment_delivery_mode
        CHECK (
            delivery_mode IN (
                'IN_PERSON',
                'ONLINE',
                'BLENDED',
                'TAKE_HOME',
                'FIELD_BASED',
                'PRACTICAL',
                'ORAL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_assessment_type_weight
        CHECK (
            default_weight_percentage IS NULL
            OR (
                default_weight_percentage >= 0
                AND default_weight_percentage <= 100
            )
        ),

    CONSTRAINT ck_gts_assessment_type_scores
        CHECK (
            (
                default_maximum_score IS NULL
                OR default_maximum_score > 0
            )
            AND (
                default_pass_score IS NULL
                OR default_pass_score >= 0
            )
            AND (
                default_maximum_score IS NULL
                OR default_pass_score IS NULL
                OR default_pass_score <= default_maximum_score
            )
        ),

    CONSTRAINT ck_gts_assessment_type_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_assessment_plan (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    plan_code VARCHAR(100) NOT NULL,
    plan_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

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

    stream_id UUID
        REFERENCES gts_stream(id),

    grading_scheme_id UUID
        REFERENCES gts_grading_scheme(id),

    effective_from DATE NOT NULL,
    effective_to DATE,

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    plan_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_assessment_plan_code
        UNIQUE (tenant_id, plan_code),

    CONSTRAINT uq_gts_assessment_plan_scope
        UNIQUE (
            tenant_id,
            academic_year_id,
            academic_term_id,
            campus_id,
            academic_programme_id,
            study_track_id,
            class_grade_id,
            stream_id
        ),

    CONSTRAINT ck_gts_assessment_plan_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_assessment_plan_approval
        CHECK (
            plan_status NOT IN ('APPROVED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_assessment_plan_lifecycle
        CHECK (
            plan_status IN (
                'DRAFT',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_assessment_plan_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_assessment_component (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    assessment_plan_id UUID NOT NULL
        REFERENCES gts_assessment_plan(id) ON DELETE CASCADE,

    component_code VARCHAR(100) NOT NULL,
    component_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    assessment_type_id UUID NOT NULL
        REFERENCES gts_assessment_type(id),

    subject_offering_id UUID NOT NULL
        REFERENCES gts_subject_offering(id),

    sequence_number INTEGER NOT NULL,

    maximum_score NUMERIC(10,2) NOT NULL,
    pass_score NUMERIC(10,2),
    weight_percentage NUMERIC(6,2) NOT NULL,

    attempt_number INTEGER NOT NULL DEFAULT 1,
    maximum_attempts INTEGER NOT NULL DEFAULT 1,

    assessment_window_start TIMESTAMPTZ,
    assessment_window_end TIMESTAMPTZ,

    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    moderation_required BOOLEAN NOT NULL DEFAULT FALSE,
    publication_allowed BOOLEAN NOT NULL DEFAULT TRUE,

    component_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_assessment_component_code
        UNIQUE (tenant_id, assessment_plan_id, component_code),

    CONSTRAINT uq_gts_assessment_component_sequence
        UNIQUE (tenant_id, assessment_plan_id, sequence_number),

    CONSTRAINT ck_gts_assessment_component_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_assessment_component_scores
        CHECK (
            maximum_score > 0
            AND (
                pass_score IS NULL
                OR (
                    pass_score >= 0
                    AND pass_score <= maximum_score
                )
            )
        ),

    CONSTRAINT ck_gts_assessment_component_weight
        CHECK (
            weight_percentage >= 0
            AND weight_percentage <= 100
        ),

    CONSTRAINT ck_gts_assessment_component_attempts
        CHECK (
            attempt_number > 0
            AND maximum_attempts > 0
            AND attempt_number <= maximum_attempts
        ),

    CONSTRAINT ck_gts_assessment_component_window
        CHECK (
            assessment_window_end IS NULL
            OR assessment_window_start IS NULL
            OR assessment_window_end >= assessment_window_start
        ),

    CONSTRAINT ck_gts_assessment_component_lifecycle
        CHECK (
            component_status IN (
                'PLANNED',
                'OPEN',
                'IN_PROGRESS',
                'MARKING',
                'MODERATION',
                'APPROVED',
                'PUBLISHED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_assessment_component_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_examination_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    session_code VARCHAR(100) NOT NULL,
    session_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    examination_type VARCHAR(40) NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    registration_open_date DATE,
    registration_close_date DATE,

    external_authority VARCHAR(250),
    external_session_reference VARCHAR(160),

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    session_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_examination_session_code
        UNIQUE (tenant_id, session_code),

    CONSTRAINT ck_gts_examination_session_type
        CHECK (
            examination_type IN (
                'INTERNAL',
                'END_OF_TERM',
                'MID_TERM',
                'MOCK',
                'NATIONAL',
                'INTERNATIONAL',
                'EXTERNAL',
                'SUPPLEMENTARY',
                'SPECIAL',
                'ENTRANCE',
                'PLACEMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_examination_session_dates
        CHECK (end_date >= start_date),

    CONSTRAINT ck_gts_examination_registration_dates
        CHECK (
            registration_close_date IS NULL
            OR registration_open_date IS NULL
            OR registration_close_date >= registration_open_date
        ),

    CONSTRAINT ck_gts_examination_session_approval
        CHECK (
            session_status NOT IN ('APPROVED', 'OPEN', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_examination_session_lifecycle
        CHECK (
            session_status IN (
                'DRAFT',
                'UNDER_REVIEW',
                'APPROVED',
                'REGISTRATION_OPEN',
                'REGISTRATION_CLOSED',
                'OPEN',
                'ACTIVE',
                'MARKING',
                'MODERATION',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_examination_session_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_assessment_paper (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    paper_code VARCHAR(100) NOT NULL,
    paper_name VARCHAR(250) NOT NULL,

    assessment_component_id UUID
        REFERENCES gts_assessment_component(id),

    examination_session_id UUID
        REFERENCES gts_examination_session(id),

    subject_offering_id UUID NOT NULL
        REFERENCES gts_subject_offering(id),

    paper_type VARCHAR(40) NOT NULL DEFAULT 'WRITTEN',

    paper_number INTEGER,
    duration_minutes INTEGER,
    maximum_score NUMERIC(10,2) NOT NULL,

    instructions VARCHAR(3000),

    eds_question_paper_document_id UUID,
    eds_marking_guide_document_id UUID,
    eds_answer_booklet_document_id UUID,

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    prepared_by UUID,
    prepared_at TIMESTAMPTZ,

    moderated_by UUID,
    moderated_at TIMESTAMPTZ,

    approved_by UUID,
    approved_at TIMESTAMPTZ,

    paper_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_assessment_paper_code
        UNIQUE (tenant_id, paper_code),

    CONSTRAINT ck_gts_assessment_paper_type
        CHECK (
            paper_type IN (
                'WRITTEN',
                'PRACTICAL',
                'ORAL',
                'PROJECT',
                'PORTFOLIO',
                'COURSEWORK',
                'ONLINE',
                'TAKE_HOME',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_assessment_paper_number
        CHECK (
            paper_number IS NULL
            OR paper_number > 0
        ),

    CONSTRAINT ck_gts_assessment_paper_duration
        CHECK (
            duration_minutes IS NULL
            OR duration_minutes > 0
        ),

    CONSTRAINT ck_gts_assessment_paper_score
        CHECK (maximum_score > 0),

    CONSTRAINT ck_gts_assessment_paper_moderation
        CHECK (
            paper_status NOT IN ('MODERATED', 'APPROVED', 'SCHEDULED', 'ADMINISTERED')
            OR moderated_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_assessment_paper_approval
        CHECK (
            paper_status NOT IN ('APPROVED', 'SCHEDULED', 'ADMINISTERED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_assessment_paper_lifecycle
        CHECK (
            paper_status IN (
                'DRAFT',
                'UNDER_REVIEW',
                'MODERATED',
                'APPROVED',
                'SCHEDULED',
                'ADMINISTERED',
                'MARKING',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_assessment_paper_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_examination_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    schedule_reference VARCHAR(100) NOT NULL,

    examination_session_id UUID NOT NULL
        REFERENCES gts_examination_session(id) ON DELETE CASCADE,

    assessment_paper_id UUID NOT NULL
        REFERENCES gts_assessment_paper(id),

    class_offering_id UUID NOT NULL
        REFERENCES gts_class_offering(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    examination_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    scheduling_resource_id UUID
        REFERENCES gts_scheduling_resource(id),

    expected_candidate_count INTEGER NOT NULL DEFAULT 0,

    timetable_entry_id UUID
        REFERENCES gts_timetable_entry(id),

    special_instructions VARCHAR(2000),

    schedule_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_examination_schedule_reference
        UNIQUE (tenant_id, schedule_reference),

    CONSTRAINT uq_gts_examination_paper_schedule
        UNIQUE (
            tenant_id,
            examination_session_id,
            assessment_paper_id,
            class_offering_id,
            stream_id
        ),

    CONSTRAINT ck_gts_examination_schedule_times
        CHECK (end_time > start_time),

    CONSTRAINT ck_gts_examination_candidate_count
        CHECK (expected_candidate_count >= 0),

    CONSTRAINT ck_gts_examination_schedule_lifecycle
        CHECK (
            schedule_status IN (
                'DRAFT',
                'SCHEDULED',
                'CONFIRMED',
                'IN_PROGRESS',
                'COMPLETED',
                'POSTPONED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_examination_schedule_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_examination_candidate (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    candidate_number VARCHAR(120) NOT NULL,

    examination_session_id UUID NOT NULL
        REFERENCES gts_examination_session(id) ON DELETE CASCADE,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,

    external_candidate_number VARCHAR(160),

    eligibility_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    eligibility_reason VARCHAR(1000),

    registered_by UUID,
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    workflow_instance_id UUID,

    candidate_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_examination_candidate_number
        UNIQUE (tenant_id, candidate_number),

    CONSTRAINT uq_gts_examination_candidate_student
        UNIQUE (tenant_id, examination_session_id, student_id),

    CONSTRAINT ck_gts_examination_candidate_eligibility
        CHECK (
            eligibility_status IN (
                'PENDING',
                'ELIGIBLE',
                'CONDITIONALLY_ELIGIBLE',
                'INELIGIBLE',
                'EXEMPTED'
            )
        ),

    CONSTRAINT ck_gts_examination_candidate_lifecycle
        CHECK (
            candidate_status IN (
                'REGISTERED',
                'VERIFIED',
                'WITHDRAWN',
                'SUSPENDED',
                'DISQUALIFIED',
                'COMPLETED',
                'ABSENT',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_examination_candidate_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_candidate_paper_registration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    examination_candidate_id UUID NOT NULL
        REFERENCES gts_examination_candidate(id) ON DELETE CASCADE,

    assessment_paper_id UUID NOT NULL
        REFERENCES gts_assessment_paper(id),

    examination_schedule_id UUID
        REFERENCES gts_examination_schedule(id),

    registration_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',

    registration_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',

    registered_at TIMESTAMPTZ NOT NULL,
    registered_by UUID,

    withdrawn_at TIMESTAMPTZ,
    withdrawal_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_candidate_paper_registration
        UNIQUE (
            tenant_id,
            examination_candidate_id,
            assessment_paper_id
        ),

    CONSTRAINT ck_gts_candidate_paper_registration_type
        CHECK (
            registration_type IN (
                'STANDARD',
                'OPTIONAL',
                'REPEAT',
                'SUPPLEMENTARY',
                'SPECIAL',
                'EXEMPTED'
            )
        ),

    CONSTRAINT ck_gts_candidate_paper_registration_lifecycle
        CHECK (
            registration_status IN (
                'REGISTERED',
                'VERIFIED',
                'WITHDRAWN',
                'ABSENT',
                'COMPLETED',
                'DISQUALIFIED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_candidate_paper_registration_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_examination_accommodation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    examination_candidate_id UUID NOT NULL
        REFERENCES gts_examination_candidate(id) ON DELETE CASCADE,

    accommodation_type VARCHAR(40) NOT NULL,
    description VARCHAR(1500),

    additional_minutes INTEGER,
    separate_room BOOLEAN NOT NULL DEFAULT FALSE,
    assistant_required BOOLEAN NOT NULL DEFAULT FALSE,
    equipment_required VARCHAR(500),

    eds_supporting_document_id UUID,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID,

    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,
    rejection_reason VARCHAR(1000),

    accommodation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_examination_accommodation_type
        CHECK (
            accommodation_type IN (
                'EXTRA_TIME',
                'SEPARATE_ROOM',
                'READER',
                'SCRIBE',
                'LARGE_PRINT',
                'BRAILLE',
                'SIGN_LANGUAGE',
                'ASSISTIVE_TECHNOLOGY',
                'REST_BREAKS',
                'MEDICAL_SUPPORT',
                'ACCESSIBLE_ROOM',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_examination_accommodation_minutes
        CHECK (
            additional_minutes IS NULL
            OR additional_minutes > 0
        ),

    CONSTRAINT ck_gts_examination_accommodation_approval
        CHECK (
            accommodation_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_examination_accommodation_lifecycle
        CHECK (
            accommodation_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_examination_accommodation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_result_publication_window (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    publication_code VARCHAR(100) NOT NULL,
    publication_name VARCHAR(250) NOT NULL,

    assessment_plan_id UUID
        REFERENCES gts_assessment_plan(id),

    examination_session_id UUID
        REFERENCES gts_examination_session(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    publication_start_at TIMESTAMPTZ NOT NULL,
    publication_end_at TIMESTAMPTZ,

    audience VARCHAR(30) NOT NULL DEFAULT 'GUARDIANS_AND_STUDENTS',

    requires_final_approval BOOLEAN NOT NULL DEFAULT TRUE,
    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    notification_required BOOLEAN NOT NULL DEFAULT TRUE,

    publication_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_result_publication_code
        UNIQUE (tenant_id, publication_code),

    CONSTRAINT ck_gts_result_publication_dates
        CHECK (
            publication_end_at IS NULL
            OR publication_end_at >= publication_start_at
        ),

    CONSTRAINT ck_gts_result_publication_audience
        CHECK (
            audience IN (
                'STUDENTS',
                'GUARDIANS',
                'GUARDIANS_AND_STUDENTS',
                'STAFF',
                'MANAGEMENT',
                'PUBLIC',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_result_publication_approval
        CHECK (
            requires_final_approval = FALSE
            OR publication_status NOT IN ('APPROVED', 'SCHEDULED', 'PUBLISHED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_result_publication_lifecycle
        CHECK (
            publication_status IN (
                'DRAFT',
                'PENDING_APPROVAL',
                'APPROVED',
                'SCHEDULED',
                'PUBLISHED',
                'WITHDRAWN',
                'EXPIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_result_publication_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_assessment_examination_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,

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

    CONSTRAINT ck_gts_assessment_history_entity
        CHECK (
            entity_type IN (
                'ASSESSMENT_TYPE',
                'ASSESSMENT_PLAN',
                'ASSESSMENT_COMPONENT',
                'EXAMINATION_SESSION',
                'ASSESSMENT_PAPER',
                'EXAMINATION_SCHEDULE',
                'EXAMINATION_CANDIDATE',
                'PAPER_REGISTRATION',
                'ACCOMMODATION',
                'PUBLICATION_WINDOW'
            )
        ),

    CONSTRAINT ck_gts_assessment_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'SUBMITTED',
                'APPROVED',
                'REJECTED',
                'OPENED',
                'CLOSED',
                'SCHEDULED',
                'RESCHEDULED',
                'REGISTERED',
                'WITHDRAWN',
                'VERIFIED',
                'ADMINISTERED',
                'COMPLETED',
                'PUBLISHED',
                'CANCELLED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_assessment_type_category
    ON gts_assessment_type (
        tenant_id,
        assessment_category,
        active
    );

CREATE INDEX ix_gts_assessment_plan_period
    ON gts_assessment_plan (
        tenant_id,
        academic_year_id,
        academic_term_id,
        class_grade_id,
        plan_status
    );

CREATE INDEX ix_gts_assessment_component_plan
    ON gts_assessment_component (
        tenant_id,
        assessment_plan_id,
        sequence_number,
        component_status
    );

CREATE INDEX ix_gts_assessment_component_subject
    ON gts_assessment_component (
        tenant_id,
        subject_offering_id,
        component_status
    );

CREATE INDEX ix_gts_examination_session_period
    ON gts_examination_session (
        tenant_id,
        academic_year_id,
        academic_term_id,
        start_date,
        session_status
    );

CREATE INDEX ix_gts_assessment_paper_subject
    ON gts_assessment_paper (
        tenant_id,
        subject_offering_id,
        paper_status
    );

CREATE INDEX ix_gts_examination_schedule_date
    ON gts_examination_schedule (
        tenant_id,
        examination_date,
        start_time,
        schedule_status
    );

CREATE INDEX ix_gts_examination_candidate_student
    ON gts_examination_candidate (
        tenant_id,
        student_id,
        examination_session_id,
        candidate_status
    );

CREATE INDEX ix_gts_candidate_paper_schedule
    ON gts_candidate_paper_registration (
        tenant_id,
        assessment_paper_id,
        examination_schedule_id,
        registration_status
    );

CREATE INDEX ix_gts_examination_accommodation_candidate
    ON gts_examination_accommodation (
        tenant_id,
        examination_candidate_id,
        accommodation_status
    );

CREATE INDEX ix_gts_result_publication_period
    ON gts_result_publication_window (
        tenant_id,
        academic_year_id,
        academic_term_id,
        publication_start_at,
        publication_status
    );

CREATE INDEX ix_gts_assessment_history_entity
    ON gts_assessment_examination_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '041',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
