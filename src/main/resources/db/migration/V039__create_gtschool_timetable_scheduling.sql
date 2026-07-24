CREATE TABLE gts_scheduling_resource (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    resource_code VARCHAR(80) NOT NULL,
    resource_name VARCHAR(200) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,

    capacity INTEGER,
    location_description VARCHAR(300),

    specialized_for_subject_id UUID
        REFERENCES gts_subject(id),

    bookable BOOLEAN NOT NULL DEFAULT TRUE,
    shared_resource BOOLEAN NOT NULL DEFAULT FALSE,

    resource_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_scheduling_resource_code
        UNIQUE (tenant_id, campus_id, resource_code),

    CONSTRAINT ck_gts_scheduling_resource_type
        CHECK (
            resource_type IN (
                'CLASSROOM',
                'LABORATORY',
                'WORKSHOP',
                'LIBRARY',
                'ICT_LAB',
                'SPORTS_FIELD',
                'HALL',
                'ASSEMBLY_AREA',
                'MUSIC_ROOM',
                'ART_ROOM',
                'SPECIAL_NEEDS_ROOM',
                'ONLINE_ROOM',
                'EQUIPMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_scheduling_resource_capacity
        CHECK (
            capacity IS NULL
            OR capacity > 0
        ),

    CONSTRAINT ck_gts_scheduling_resource_lifecycle
        CHECK (
            resource_status IN (
                'PLANNED',
                'ACTIVE',
                'MAINTENANCE',
                'UNAVAILABLE',
                'RETIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_scheduling_resource_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_bell_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    schedule_code VARCHAR(80) NOT NULL,
    schedule_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    schedule_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR',

    effective_from DATE NOT NULL,
    effective_to DATE,

    monday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tuesday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    wednesday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    thursday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    friday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    saturday_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    sunday_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    schedule_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_bell_schedule_code
        UNIQUE (tenant_id, campus_id, schedule_code),

    CONSTRAINT ck_gts_bell_schedule_type
        CHECK (
            schedule_type IN (
                'REGULAR',
                'EXAMINATION',
                'SHORT_DAY',
                'WEEKEND',
                'BOARDING',
                'HOLIDAY_PROGRAMME',
                'EMERGENCY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_bell_schedule_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_bell_schedule_lifecycle
        CHECK (
            schedule_status IN (
                'DRAFT',
                'APPROVED',
                'ACTIVE',
                'SUSPENDED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_bell_schedule_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_active_bell_schedule
    ON gts_bell_schedule (tenant_id, campus_id, schedule_type)
    WHERE schedule_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE gts_bell_period (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    bell_schedule_id UUID NOT NULL
        REFERENCES gts_bell_schedule(id) ON DELETE CASCADE,

    period_code VARCHAR(60) NOT NULL,
    period_name VARCHAR(160) NOT NULL,
    sequence_number INTEGER NOT NULL,

    period_type VARCHAR(30) NOT NULL DEFAULT 'TEACHING',

    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    instructional_minutes INTEGER,
    attendance_required BOOLEAN NOT NULL DEFAULT TRUE,
    scheduling_allowed BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_bell_period_code
        UNIQUE (tenant_id, bell_schedule_id, period_code),

    CONSTRAINT uq_gts_bell_period_sequence
        UNIQUE (tenant_id, bell_schedule_id, sequence_number),

    CONSTRAINT ck_gts_bell_period_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_bell_period_type
        CHECK (
            period_type IN (
                'TEACHING',
                'BREAK',
                'LUNCH',
                'ASSEMBLY',
                'REGISTRATION',
                'STUDY',
                'SPORTS',
                'CLUB',
                'WORSHIP',
                'BOARDING',
                'EXAMINATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_bell_period_times
        CHECK (end_time > start_time),

    CONSTRAINT ck_gts_bell_period_minutes
        CHECK (
            instructional_minutes IS NULL
            OR instructional_minutes >= 0
        ),

    CONSTRAINT ck_gts_bell_period_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_timetable (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    timetable_code VARCHAR(100) NOT NULL,
    timetable_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    bell_schedule_id UUID NOT NULL
        REFERENCES gts_bell_schedule(id),

    timetable_type VARCHAR(30) NOT NULL DEFAULT 'MASTER',
    version_number INTEGER NOT NULL DEFAULT 1,

    effective_from DATE NOT NULL,
    effective_to DATE,

    generated_by VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    generation_reference UUID,

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    published_at TIMESTAMPTZ,
    published_by UUID,

    timetable_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_timetable_code
        UNIQUE (tenant_id, timetable_code),

    CONSTRAINT uq_gts_timetable_version
        UNIQUE (
            tenant_id,
            academic_year_id,
            academic_term_id,
            campus_id,
            timetable_type,
            version_number
        ),

    CONSTRAINT ck_gts_timetable_type
        CHECK (
            timetable_type IN (
                'MASTER',
                'CLASS',
                'TEACHER',
                'ROOM',
                'EXAMINATION',
                'BOARDING',
                'ACTIVITY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_timetable_version_number
        CHECK (version_number > 0),

    CONSTRAINT ck_gts_timetable_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_timetable_generated_by
        CHECK (
            generated_by IN (
                'MANUAL',
                'RULE_ENGINE',
                'AI_ASSISTED',
                'IMPORTED'
            )
        ),

    CONSTRAINT ck_gts_timetable_approval
        CHECK (
            timetable_status NOT IN ('APPROVED', 'PUBLISHED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_timetable_publication
        CHECK (
            timetable_status NOT IN ('PUBLISHED', 'ACTIVE')
            OR published_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_timetable_lifecycle
        CHECK (
            timetable_status IN (
                'DRAFT',
                'GENERATED',
                'UNDER_REVIEW',
                'APPROVED',
                'PUBLISHED',
                'ACTIVE',
                'SUPERSEDED',
                'SUSPENDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_timetable_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_active_timetable
    ON gts_timetable (
        tenant_id,
        academic_year_id,
        academic_term_id,
        campus_id,
        timetable_type
    )
    WHERE timetable_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE gts_timetable_entry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    timetable_id UUID NOT NULL
        REFERENCES gts_timetable(id) ON DELETE CASCADE,

    bell_period_id UUID NOT NULL
        REFERENCES gts_bell_period(id),

    day_of_week VARCHAR(15) NOT NULL,

    class_offering_id UUID
        REFERENCES gts_class_offering(id),

    subject_offering_id UUID
        REFERENCES gts_subject_offering(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    teaching_assignment_id UUID
        REFERENCES gts_teaching_assignment(id),

    teacher_profile_id UUID
        REFERENCES gts_teacher_profile(id),

    scheduling_resource_id UUID
        REFERENCES gts_scheduling_resource(id),

    entry_type VARCHAR(30) NOT NULL DEFAULT 'LESSON',

    activity_name VARCHAR(250),
    notes VARCHAR(1000),

    recurring BOOLEAN NOT NULL DEFAULT TRUE,
    recurrence_rule VARCHAR(500),

    effective_from DATE,
    effective_to DATE,

    entry_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_timetable_entry_day
        CHECK (
            day_of_week IN (
                'MONDAY',
                'TUESDAY',
                'WEDNESDAY',
                'THURSDAY',
                'FRIDAY',
                'SATURDAY',
                'SUNDAY'
            )
        ),

    CONSTRAINT ck_gts_timetable_entry_type
        CHECK (
            entry_type IN (
                'LESSON',
                'BREAK',
                'LUNCH',
                'ASSEMBLY',
                'STUDY',
                'SPORTS',
                'CLUB',
                'WORSHIP',
                'EXAMINATION',
                'COUNSELLING',
                'SPECIAL_SUPPORT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_timetable_entry_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_timetable_entry_lifecycle
        CHECK (
            entry_status IN (
                'SCHEDULED',
                'CONFIRMED',
                'ACTIVE',
                'COMPLETED',
                'CANCELLED',
                'MOVED',
                'REPLACED'
            )
        ),

    CONSTRAINT ck_gts_timetable_entry_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_timetable_class_slot
    ON gts_timetable_entry (
        tenant_id,
        timetable_id,
        day_of_week,
        bell_period_id,
        class_grade_id,
        stream_id
    )
    WHERE entry_status IN ('SCHEDULED', 'CONFIRMED', 'ACTIVE')
      AND status = 'ACTIVE'
      AND class_grade_id IS NOT NULL;

CREATE UNIQUE INDEX uq_gts_timetable_teacher_slot
    ON gts_timetable_entry (
        tenant_id,
        timetable_id,
        day_of_week,
        bell_period_id,
        teacher_profile_id
    )
    WHERE entry_status IN ('SCHEDULED', 'CONFIRMED', 'ACTIVE')
      AND status = 'ACTIVE'
      AND teacher_profile_id IS NOT NULL;

CREATE UNIQUE INDEX uq_gts_timetable_resource_slot
    ON gts_timetable_entry (
        tenant_id,
        timetable_id,
        day_of_week,
        bell_period_id,
        scheduling_resource_id
    )
    WHERE entry_status IN ('SCHEDULED', 'CONFIRMED', 'ACTIVE')
      AND status = 'ACTIVE'
      AND scheduling_resource_id IS NOT NULL;

CREATE TABLE gts_timetable_exception (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    timetable_entry_id UUID NOT NULL
        REFERENCES gts_timetable_entry(id) ON DELETE CASCADE,

    exception_date DATE NOT NULL,
    exception_type VARCHAR(30) NOT NULL,

    replacement_teacher_profile_id UUID
        REFERENCES gts_teacher_profile(id),

    replacement_resource_id UUID
        REFERENCES gts_scheduling_resource(id),

    replacement_bell_period_id UUID
        REFERENCES gts_bell_period(id),

    reason VARCHAR(1000),

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    exception_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_timetable_exception_date
        UNIQUE (
            tenant_id,
            timetable_entry_id,
            exception_date
        ),

    CONSTRAINT ck_gts_timetable_exception_type
        CHECK (
            exception_type IN (
                'CANCELLED',
                'RESCHEDULED',
                'TEACHER_SUBSTITUTION',
                'ROOM_CHANGE',
                'PERIOD_CHANGE',
                'SPECIAL_EVENT',
                'EMERGENCY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_timetable_exception_lifecycle
        CHECK (
            exception_status IN (
                'PENDING',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_timetable_exception_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_resource_unavailability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    scheduling_resource_id UUID NOT NULL
        REFERENCES gts_scheduling_resource(id) ON DELETE CASCADE,

    unavailable_from TIMESTAMPTZ NOT NULL,
    unavailable_to TIMESTAMPTZ NOT NULL,

    reason_type VARCHAR(30) NOT NULL,
    reason VARCHAR(1000),

    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_rule VARCHAR(500),

    workflow_instance_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_resource_unavailability_dates
        CHECK (unavailable_to > unavailable_from),

    CONSTRAINT ck_gts_resource_unavailability_reason
        CHECK (
            reason_type IN (
                'MAINTENANCE',
                'REPAIR',
                'RESERVED',
                'SAFETY',
                'EVENT',
                'CAPACITY_RESTRICTION',
                'EMERGENCY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_resource_unavailability_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_teacher_unavailability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    teacher_profile_id UUID NOT NULL
        REFERENCES gts_teacher_profile(id) ON DELETE CASCADE,

    unavailable_from TIMESTAMPTZ NOT NULL,
    unavailable_to TIMESTAMPTZ NOT NULL,

    reason_type VARCHAR(30) NOT NULL,
    reason VARCHAR(1000),

    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_rule VARCHAR(500),

    leave_extension_id UUID
        REFERENCES gts_teacher_leave_extension(id),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_teacher_unavailability_dates
        CHECK (unavailable_to > unavailable_from),

    CONSTRAINT ck_gts_teacher_unavailability_reason
        CHECK (
            reason_type IN (
                'LEAVE',
                'MEETING',
                'TRAINING',
                'EXAMINATION_DUTY',
                'OFFICIAL_DUTY',
                'MEDICAL',
                'RESTRICTION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_teacher_unavailability_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_timetable_conflict (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    timetable_id UUID NOT NULL
        REFERENCES gts_timetable(id) ON DELETE CASCADE,

    timetable_entry_id UUID
        REFERENCES gts_timetable_entry(id) ON DELETE CASCADE,

    conflicting_entry_id UUID
        REFERENCES gts_timetable_entry(id) ON DELETE CASCADE,

    conflict_type VARCHAR(40) NOT NULL,
    conflict_severity VARCHAR(20) NOT NULL DEFAULT 'ERROR',

    conflict_description VARCHAR(1500) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL,

    detected_by VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',

    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMPTZ,
    resolved_by UUID,
    resolution_notes VARCHAR(1500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_timetable_conflict_type
        CHECK (
            conflict_type IN (
                'TEACHER_DOUBLE_BOOKING',
                'CLASS_DOUBLE_BOOKING',
                'ROOM_DOUBLE_BOOKING',
                'RESOURCE_UNAVAILABLE',
                'TEACHER_UNAVAILABLE',
                'CAPACITY_EXCEEDED',
                'SUBJECT_PERIOD_LIMIT',
                'WORKLOAD_EXCEEDED',
                'OUTSIDE_BELL_SCHEDULE',
                'CURRICULUM_MISMATCH',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_timetable_conflict_severity
        CHECK (
            conflict_severity IN (
                'INFO',
                'WARNING',
                'ERROR',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_timetable_conflict_detector
        CHECK (
            detected_by IN (
                'SYSTEM',
                'RULE_ENGINE',
                'AI_ASSISTED',
                'MANUAL'
            )
        ),

    CONSTRAINT ck_gts_timetable_conflict_resolution
        CHECK (
            resolved = FALSE
            OR resolved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_timetable_conflict_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_timetable_change_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    timetable_id UUID NOT NULL
        REFERENCES gts_timetable(id) ON DELETE CASCADE,

    timetable_entry_id UUID
        REFERENCES gts_timetable_entry(id) ON DELETE CASCADE,

    change_type VARCHAR(40) NOT NULL,
    change_reason VARCHAR(1500),

    previous_value JSONB,
    new_value JSONB,

    effective_at TIMESTAMPTZ NOT NULL,
    changed_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    notification_sent_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_timetable_change_type
        CHECK (
            change_type IN (
                'TIMETABLE_CREATED',
                'TIMETABLE_GENERATED',
                'TIMETABLE_APPROVED',
                'TIMETABLE_PUBLISHED',
                'ENTRY_ADDED',
                'ENTRY_UPDATED',
                'ENTRY_MOVED',
                'ENTRY_CANCELLED',
                'TEACHER_CHANGED',
                'ROOM_CHANGED',
                'PERIOD_CHANGED',
                'CONFLICT_DETECTED',
                'CONFLICT_RESOLVED',
                'VERSION_SUPERSEDED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_scheduling_resource_campus
    ON gts_scheduling_resource (
        tenant_id,
        campus_id,
        resource_type,
        resource_status
    );

CREATE INDEX ix_gts_bell_schedule_campus
    ON gts_bell_schedule (
        tenant_id,
        campus_id,
        schedule_status
    );

CREATE INDEX ix_gts_bell_period_schedule
    ON gts_bell_period (
        tenant_id,
        bell_schedule_id,
        sequence_number
    );

CREATE INDEX ix_gts_timetable_academic_period
    ON gts_timetable (
        tenant_id,
        academic_year_id,
        academic_term_id,
        campus_id,
        timetable_status
    );

CREATE INDEX ix_gts_timetable_entry_day
    ON gts_timetable_entry (
        tenant_id,
        timetable_id,
        day_of_week,
        bell_period_id
    );

CREATE INDEX ix_gts_timetable_entry_teacher
    ON gts_timetable_entry (
        tenant_id,
        teacher_profile_id,
        day_of_week,
        bell_period_id
    )
    WHERE teacher_profile_id IS NOT NULL;

CREATE INDEX ix_gts_timetable_entry_class
    ON gts_timetable_entry (
        tenant_id,
        class_grade_id,
        stream_id,
        day_of_week,
        bell_period_id
    )
    WHERE class_grade_id IS NOT NULL;

CREATE INDEX ix_gts_timetable_exception_date
    ON gts_timetable_exception (
        tenant_id,
        exception_date,
        exception_status
    );

CREATE INDEX ix_gts_resource_unavailability_period
    ON gts_resource_unavailability (
        tenant_id,
        scheduling_resource_id,
        unavailable_from,
        unavailable_to
    );

CREATE INDEX ix_gts_teacher_unavailability_period
    ON gts_teacher_unavailability (
        tenant_id,
        teacher_profile_id,
        unavailable_from,
        unavailable_to
    );

CREATE INDEX ix_gts_timetable_conflict_status
    ON gts_timetable_conflict (
        tenant_id,
        timetable_id,
        resolved,
        conflict_severity
    );

CREATE INDEX ix_gts_timetable_change_history
    ON gts_timetable_change_history (
        tenant_id,
        timetable_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '039',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
