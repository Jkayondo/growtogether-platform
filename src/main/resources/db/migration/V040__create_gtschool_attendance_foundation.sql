CREATE TABLE gts_attendance_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    session_reference VARCHAR(100) NOT NULL,

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

    timetable_entry_id UUID
        REFERENCES gts_timetable_entry(id),

    subject_offering_id UUID
        REFERENCES gts_subject_offering(id),

    teaching_assignment_id UUID
        REFERENCES gts_teaching_assignment(id),

    teacher_profile_id UUID
        REFERENCES gts_teacher_profile(id),

    attendance_date DATE NOT NULL,
    session_type VARCHAR(30) NOT NULL DEFAULT 'DAILY_REGISTER',

    scheduled_start_time TIME,
    scheduled_end_time TIME,
    actual_start_time TIME,
    actual_end_time TIME,

    register_opened_at TIMESTAMPTZ,
    register_opened_by UUID,
    register_closed_at TIMESTAMPTZ,
    register_closed_by UUID,

    expected_student_count INTEGER NOT NULL DEFAULT 0,
    recorded_student_count INTEGER NOT NULL DEFAULT 0,

    workflow_instance_id UUID,

    session_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_attendance_session_reference
        UNIQUE (tenant_id, session_reference),

    CONSTRAINT uq_gts_attendance_session_scope
        UNIQUE (
            tenant_id,
            attendance_date,
            timetable_entry_id,
            class_grade_id,
            stream_id,
            session_type
        ),

    CONSTRAINT ck_gts_attendance_session_type
        CHECK (
            session_type IN (
                'DAILY_REGISTER',
                'LESSON',
                'ASSEMBLY',
                'BOARDING_MORNING',
                'BOARDING_EVENING',
                'EXAMINATION',
                'ACTIVITY',
                'TRANSPORT',
                'SPECIAL_EVENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_attendance_session_schedule_times
        CHECK (
            scheduled_end_time IS NULL
            OR scheduled_start_time IS NULL
            OR scheduled_end_time > scheduled_start_time
        ),

    CONSTRAINT ck_gts_attendance_session_actual_times
        CHECK (
            actual_end_time IS NULL
            OR actual_start_time IS NULL
            OR actual_end_time > actual_start_time
        ),

    CONSTRAINT ck_gts_attendance_session_counts
        CHECK (
            expected_student_count >= 0
            AND recorded_student_count >= 0
            AND recorded_student_count <= expected_student_count
        ),

    CONSTRAINT ck_gts_attendance_session_closed
        CHECK (
            session_status <> 'CLOSED'
            OR register_closed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_attendance_session_lifecycle
        CHECK (
            session_status IN (
                'PLANNED',
                'OPEN',
                'IN_PROGRESS',
                'PENDING_REVIEW',
                'CLOSED',
                'REOPENED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_attendance_session_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    attendance_session_id UUID NOT NULL
        REFERENCES gts_attendance_session(id) ON DELETE CASCADE,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    attendance_status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',

    scheduled_arrival_time TIME,
    actual_arrival_time TIME,
    scheduled_departure_time TIME,
    actual_departure_time TIME,

    minutes_late INTEGER NOT NULL DEFAULT 0,
    minutes_early_departure INTEGER NOT NULL DEFAULT 0,

    attendance_reason_id UUID,
    reason_notes VARCHAR(1000),

    recorded_at TIMESTAMPTZ NOT NULL,
    recorded_by UUID,

    source_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    source_reference VARCHAR(160),

    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    notification_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUIRED',
    notification_request_id UUID,
    notification_sent_at TIMESTAMPTZ,

    workflow_instance_id UUID,

    record_status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_attendance_session
        UNIQUE (tenant_id, attendance_session_id, student_id),

    CONSTRAINT ck_gts_student_attendance_status
        CHECK (
            attendance_status IN (
                'PRESENT',
                'ABSENT',
                'LATE',
                'EXCUSED_ABSENCE',
                'UNEXCUSED_ABSENCE',
                'MEDICAL_ABSENCE',
                'SCHOOL_ACTIVITY',
                'REMOTE_LEARNING',
                'EARLY_DEPARTURE',
                'SUSPENDED',
                'NOT_REQUIRED',
                'UNKNOWN'
            )
        ),

    CONSTRAINT ck_gts_student_attendance_late
        CHECK (minutes_late >= 0),

    CONSTRAINT ck_gts_student_attendance_early_departure
        CHECK (minutes_early_departure >= 0),

    CONSTRAINT ck_gts_student_attendance_source
        CHECK (
            source_type IN (
                'MANUAL',
                'BIOMETRIC',
                'RFID',
                'QR_CODE',
                'MOBILE',
                'IMPORT',
                'TRANSPORT',
                'SYSTEM'
            )
        ),

    CONSTRAINT ck_gts_student_attendance_verification
        CHECK (
            verified = FALSE
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_attendance_notification
        CHECK (
            notification_status IN (
                'NOT_REQUIRED',
                'PENDING',
                'QUEUED',
                'SENT',
                'DELIVERED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_student_attendance_record_lifecycle
        CHECK (
            record_status IN (
                'RECORDED',
                'PENDING_REVIEW',
                'CONFIRMED',
                'CORRECTED',
                'DISPUTED',
                'VOIDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_attendance_status_record
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_attendance_reason (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    reason_code VARCHAR(80) NOT NULL,
    reason_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    reason_category VARCHAR(40) NOT NULL,
    excused BOOLEAN NOT NULL DEFAULT FALSE,
    evidence_required BOOLEAN NOT NULL DEFAULT FALSE,
    approval_required BOOLEAN NOT NULL DEFAULT FALSE,
    notification_required BOOLEAN NOT NULL DEFAULT FALSE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_attendance_reason_code
        UNIQUE (tenant_id, reason_code),

    CONSTRAINT ck_gts_attendance_reason_category
        CHECK (
            reason_category IN (
                'ILLNESS',
                'MEDICAL_APPOINTMENT',
                'FAMILY_EMERGENCY',
                'BEREAVEMENT',
                'TRANSPORT_DELAY',
                'WEATHER',
                'SCHOOL_ACTIVITY',
                'RELIGIOUS_OBSERVANCE',
                'DISCIPLINARY',
                'AUTHORIZED_LEAVE',
                'UNAUTHORIZED',
                'LATE_ARRIVAL',
                'EARLY_DEPARTURE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_attendance_reason_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

ALTER TABLE gts_student_attendance
    ADD CONSTRAINT fk_gts_student_attendance_reason
    FOREIGN KEY (attendance_reason_id)
    REFERENCES gts_attendance_reason(id);

CREATE TABLE gts_attendance_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_attendance_id UUID NOT NULL
        REFERENCES gts_student_attendance(id) ON DELETE CASCADE,

    evidence_type VARCHAR(40) NOT NULL,
    eds_document_id UUID,
    evidence_reference VARCHAR(160),
    evidence_description VARCHAR(1000),

    submitted_at TIMESTAMPTZ NOT NULL,
    submitted_by UUID,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    rejection_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_attendance_evidence_type
        CHECK (
            evidence_type IN (
                'MEDICAL_NOTE',
                'PARENT_LETTER',
                'OFFICIAL_LETTER',
                'TRANSPORT_REPORT',
                'ACTIVITY_AUTHORIZATION',
                'PHOTO',
                'SYSTEM_RECORD',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_attendance_evidence_verification
        CHECK (
            verification_status IN (
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'WAIVED'
            )
        ),

    CONSTRAINT ck_gts_attendance_evidence_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_attendance_evidence_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_attendance_correction_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    correction_reference VARCHAR(100) NOT NULL,

    student_attendance_id UUID NOT NULL
        REFERENCES gts_student_attendance(id),

    requested_status VARCHAR(30) NOT NULL,
    requested_reason_id UUID
        REFERENCES gts_attendance_reason(id),

    request_reason VARCHAR(1500) NOT NULL,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID NOT NULL,

    original_value JSONB NOT NULL,
    requested_value JSONB NOT NULL,

    workflow_instance_id UUID,
    workflow_task_id UUID,

    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,
    decision_notes VARCHAR(1500),

    correction_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    applied_at TIMESTAMPTZ,
    applied_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_attendance_correction_reference
        UNIQUE (tenant_id, correction_reference),

    CONSTRAINT ck_gts_attendance_correction_requested_status
        CHECK (
            requested_status IN (
                'PRESENT',
                'ABSENT',
                'LATE',
                'EXCUSED_ABSENCE',
                'UNEXCUSED_ABSENCE',
                'MEDICAL_ABSENCE',
                'SCHOOL_ACTIVITY',
                'REMOTE_LEARNING',
                'EARLY_DEPARTURE',
                'SUSPENDED',
                'NOT_REQUIRED',
                'UNKNOWN'
            )
        ),

    CONSTRAINT ck_gts_attendance_correction_lifecycle
        CHECK (
            correction_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_attendance_correction_applied
        CHECK (
            correction_status <> 'APPLIED'
            OR applied_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_attendance_correction_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_late_arrival (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_attendance_id UUID NOT NULL
        REFERENCES gts_student_attendance(id) ON DELETE CASCADE,

    arrival_date DATE NOT NULL,
    expected_arrival_time TIME,
    actual_arrival_time TIME NOT NULL,
    minutes_late INTEGER NOT NULL,

    attendance_reason_id UUID
        REFERENCES gts_attendance_reason(id),

    arrival_location VARCHAR(160),
    received_by UUID,

    parent_notified BOOLEAN NOT NULL DEFAULT FALSE,
    parent_notification_request_id UUID,
    parent_notified_at TIMESTAMPTZ,

    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_notes VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_late_arrival_attendance
        UNIQUE (tenant_id, student_attendance_id),

    CONSTRAINT ck_gts_student_late_arrival_minutes
        CHECK (minutes_late > 0),

    CONSTRAINT ck_gts_student_late_arrival_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_early_departure (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_attendance_id UUID NOT NULL
        REFERENCES gts_student_attendance(id) ON DELETE CASCADE,

    departure_date DATE NOT NULL,
    scheduled_departure_time TIME,
    actual_departure_time TIME NOT NULL,
    minutes_early INTEGER NOT NULL,

    attendance_reason_id UUID
        REFERENCES gts_attendance_reason(id),

    requested_by_guardian_id UUID
        REFERENCES gts_guardian(id),

    authorized_pickup_id UUID
        REFERENCES gts_student_pickup_authorization(id),

    released_by UUID,
    release_location VARCHAR(160),

    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    workflow_instance_id UUID,

    parent_notification_request_id UUID,
    parent_notified_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_early_departure_attendance
        UNIQUE (tenant_id, student_attendance_id),

    CONSTRAINT ck_gts_student_early_departure_minutes
        CHECK (minutes_early > 0),

    CONSTRAINT ck_gts_student_early_departure_approval
        CHECK (
            approval_required = FALSE
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_early_departure_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_attendance_notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_attendance_id UUID NOT NULL
        REFERENCES gts_student_attendance(id) ON DELETE CASCADE,

    guardian_id UUID
        REFERENCES gts_guardian(id),

    communication_channel VARCHAR(30) NOT NULL,
    notification_type VARCHAR(40) NOT NULL,

    notification_request_id UUID,
    destination VARCHAR(250),

    notification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    queued_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    failure_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_attendance_notification_channel
        CHECK (
            communication_channel IN (
                'SMS',
                'EMAIL',
                'PUSH',
                'WHATSAPP',
                'VOICE_CALL',
                'IN_APP'
            )
        ),

    CONSTRAINT ck_gts_attendance_notification_type
        CHECK (
            notification_type IN (
                'ABSENCE',
                'LATE_ARRIVAL',
                'EARLY_DEPARTURE',
                'ATTENDANCE_CORRECTION',
                'REPEATED_ABSENCE',
                'LOW_ATTENDANCE',
                'EMERGENCY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_attendance_notification_lifecycle
        CHECK (
            notification_status IN (
                'PENDING',
                'QUEUED',
                'SENT',
                'DELIVERED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_attendance_notification_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_attendance_threshold_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    rule_code VARCHAR(100) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    description VARCHAR(1200),

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    threshold_type VARCHAR(40) NOT NULL,
    threshold_value NUMERIC(8,2) NOT NULL,
    evaluation_period VARCHAR(30) NOT NULL,

    consequence_type VARCHAR(40),
    notification_required BOOLEAN NOT NULL DEFAULT TRUE,
    workflow_required BOOLEAN NOT NULL DEFAULT FALSE,

    workflow_definition_reference UUID,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_attendance_threshold_rule_code
        UNIQUE (tenant_id, rule_code),

    CONSTRAINT ck_gts_attendance_threshold_type
        CHECK (
            threshold_type IN (
                'MINIMUM_ATTENDANCE_PERCENTAGE',
                'MAXIMUM_ABSENCE_COUNT',
                'MAXIMUM_LATE_COUNT',
                'CONSECUTIVE_ABSENCE_COUNT',
                'MAXIMUM_EARLY_DEPARTURE_COUNT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_attendance_threshold_value
        CHECK (threshold_value >= 0),

    CONSTRAINT ck_gts_attendance_evaluation_period
        CHECK (
            evaluation_period IN (
                'DAILY',
                'WEEKLY',
                'MONTHLY',
                'TERM',
                'ACADEMIC_YEAR',
                'ROLLING_30_DAYS',
                'ROLLING_90_DAYS'
            )
        ),

    CONSTRAINT ck_gts_attendance_consequence_type
        CHECK (
            consequence_type IS NULL
            OR consequence_type IN (
                'INFORMATION',
                'WARNING',
                'PARENT_MEETING',
                'COUNSELLING',
                'DISCIPLINE_REVIEW',
                'ACADEMIC_REVIEW',
                'WELFARE_REVIEW',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_attendance_threshold_rule_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_attendance_summary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    summary_period_start DATE NOT NULL,
    summary_period_end DATE NOT NULL,

    expected_sessions INTEGER NOT NULL DEFAULT 0,
    present_sessions INTEGER NOT NULL DEFAULT 0,
    absent_sessions INTEGER NOT NULL DEFAULT 0,
    excused_absence_sessions INTEGER NOT NULL DEFAULT 0,
    unexcused_absence_sessions INTEGER NOT NULL DEFAULT 0,
    late_sessions INTEGER NOT NULL DEFAULT 0,
    early_departure_sessions INTEGER NOT NULL DEFAULT 0,

    attendance_percentage NUMERIC(6,2),

    threshold_breached BOOLEAN NOT NULL DEFAULT FALSE,
    threshold_rule_id UUID
        REFERENCES gts_attendance_threshold_rule(id),

    calculated_at TIMESTAMPTZ NOT NULL,
    calculation_reference VARCHAR(160),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_attendance_summary
        UNIQUE (
            tenant_id,
            student_id,
            academic_year_id,
            academic_term_id,
            summary_period_start,
            summary_period_end
        ),

    CONSTRAINT ck_gts_student_attendance_summary_dates
        CHECK (summary_period_end >= summary_period_start),

    CONSTRAINT ck_gts_student_attendance_summary_counts
        CHECK (
            expected_sessions >= 0
            AND present_sessions >= 0
            AND absent_sessions >= 0
            AND excused_absence_sessions >= 0
            AND unexcused_absence_sessions >= 0
            AND late_sessions >= 0
            AND early_departure_sessions >= 0
        ),

    CONSTRAINT ck_gts_student_attendance_summary_percentage
        CHECK (
            attendance_percentage IS NULL
            OR (
                attendance_percentage >= 0
                AND attendance_percentage <= 100
            )
        ),

    CONSTRAINT ck_gts_student_attendance_summary_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_attendance_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_attendance_id UUID NOT NULL
        REFERENCES gts_student_attendance(id) ON DELETE CASCADE,

    event_type VARCHAR(40) NOT NULL,

    previous_value JSONB,
    new_value JSONB,

    event_reason VARCHAR(1500),
    effective_at TIMESTAMPTZ NOT NULL,
    event_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_attendance_history_event
        CHECK (
            event_type IN (
                'RECORDED',
                'VERIFIED',
                'CORRECTION_REQUESTED',
                'CORRECTION_APPROVED',
                'CORRECTION_REJECTED',
                'CORRECTED',
                'DISPUTED',
                'EVIDENCE_ADDED',
                'NOTIFICATION_QUEUED',
                'NOTIFICATION_SENT',
                'NOTIFICATION_FAILED',
                'VOIDED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_attendance_session_date
    ON gts_attendance_session (
        tenant_id,
        attendance_date,
        campus_id,
        session_status
    );

CREATE INDEX ix_gts_attendance_session_class
    ON gts_attendance_session (
        tenant_id,
        class_grade_id,
        stream_id,
        attendance_date
    );

CREATE INDEX ix_gts_student_attendance_student
    ON gts_student_attendance (
        tenant_id,
        student_id,
        attendance_status,
        recorded_at
    );

CREATE INDEX ix_gts_student_attendance_session
    ON gts_student_attendance (
        tenant_id,
        attendance_session_id,
        record_status
    );

CREATE INDEX ix_gts_attendance_reason_category
    ON gts_attendance_reason (
        tenant_id,
        reason_category,
        active
    );

CREATE INDEX ix_gts_attendance_evidence_record
    ON gts_attendance_evidence (
        tenant_id,
        student_attendance_id,
        verification_status
    );

CREATE INDEX ix_gts_attendance_correction_record
    ON gts_attendance_correction_request (
        tenant_id,
        student_attendance_id,
        correction_status
    );

CREATE INDEX ix_gts_late_arrival_date
    ON gts_student_late_arrival (
        tenant_id,
        arrival_date,
        minutes_late
    );

CREATE INDEX ix_gts_early_departure_date
    ON gts_student_early_departure (
        tenant_id,
        departure_date,
        minutes_early
    );

CREATE INDEX ix_gts_attendance_notification_record
    ON gts_attendance_notification (
        tenant_id,
        student_attendance_id,
        notification_status
    );

CREATE INDEX ix_gts_attendance_threshold_scope
    ON gts_attendance_threshold_rule (
        tenant_id,
        academic_programme_id,
        class_grade_id,
        active
    );

CREATE INDEX ix_gts_attendance_summary_student
    ON gts_student_attendance_summary (
        tenant_id,
        student_id,
        academic_year_id,
        academic_term_id
    );

CREATE INDEX ix_gts_attendance_summary_threshold
    ON gts_student_attendance_summary (
        tenant_id,
        threshold_breached,
        calculated_at
    );

CREATE INDEX ix_gts_attendance_history_record
    ON gts_attendance_history (
        tenant_id,
        student_attendance_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '040',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
