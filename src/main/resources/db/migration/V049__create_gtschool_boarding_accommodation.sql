CREATE TABLE gts_boarding_house (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    house_code VARCHAR(80) NOT NULL,
    house_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    house_type VARCHAR(40) NOT NULL,
    gender_policy VARCHAR(30) NOT NULL DEFAULT 'MIXED',

    maximum_capacity INTEGER NOT NULL,
    minimum_age INTEGER,
    maximum_age INTEGER,

    location_description VARCHAR(300),

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    deputy_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    health_facility_id UUID
        REFERENCES gts_health_facility(id),

    emergency_assembly_location VARCHAR(300),

    house_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_house_code
        UNIQUE (tenant_id, campus_id, house_code),

    CONSTRAINT ck_gts_boarding_house_type
        CHECK (
            house_type IN (
                'DORMITORY',
                'HOSTEL',
                'RESIDENTIAL_HOUSE',
                'SPECIAL_NEEDS_UNIT',
                'SICKBAY_RESIDENTIAL',
                'STAFF_QUARTERS',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_house_gender
        CHECK (
            gender_policy IN (
                'MALE',
                'FEMALE',
                'MIXED',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_house_capacity
        CHECK (maximum_capacity > 0),

    CONSTRAINT ck_gts_boarding_house_ages
        CHECK (
            (minimum_age IS NULL OR minimum_age >= 0)
            AND (maximum_age IS NULL OR maximum_age >= 0)
            AND (
                minimum_age IS NULL
                OR maximum_age IS NULL
                OR maximum_age >= minimum_age
            )
        ),

    CONSTRAINT ck_gts_boarding_house_lifecycle
        CHECK (
            house_status IN (
                'PLANNED',
                'ACTIVE',
                'FULL',
                'TEMPORARILY_CLOSED',
                'MAINTENANCE',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_house_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id) ON DELETE CASCADE,

    room_code VARCHAR(80) NOT NULL,
    room_name VARCHAR(200),

    room_type VARCHAR(40) NOT NULL DEFAULT 'DORMITORY_ROOM',

    floor_number INTEGER,
    maximum_capacity INTEGER NOT NULL,

    accessible_room BOOLEAN NOT NULL DEFAULT FALSE,
    isolation_capable BOOLEAN NOT NULL DEFAULT FALSE,

    location_description VARCHAR(300),

    room_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_room_code
        UNIQUE (tenant_id, boarding_house_id, room_code),

    CONSTRAINT ck_gts_boarding_room_type
        CHECK (
            room_type IN (
                'DORMITORY_ROOM',
                'PRIVATE_ROOM',
                'SHARED_ROOM',
                'SPECIAL_NEEDS_ROOM',
                'ISOLATION_ROOM',
                'WARDEN_ROOM',
                'VISITOR_ROOM',
                'STORE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_room_floor
        CHECK (
            floor_number IS NULL
            OR floor_number >= -5
        ),

    CONSTRAINT ck_gts_boarding_room_capacity
        CHECK (maximum_capacity > 0),

    CONSTRAINT ck_gts_boarding_room_lifecycle
        CHECK (
            room_status IN (
                'AVAILABLE',
                'PARTIALLY_OCCUPIED',
                'FULL',
                'RESERVED',
                'MAINTENANCE',
                'UNAVAILABLE',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_room_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_bed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    boarding_room_id UUID NOT NULL
        REFERENCES gts_boarding_room(id) ON DELETE CASCADE,

    bed_code VARCHAR(80) NOT NULL,
    bed_label VARCHAR(160),

    bed_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',

    accessible_bed BOOLEAN NOT NULL DEFAULT FALSE,
    upper_bunk BOOLEAN NOT NULL DEFAULT FALSE,

    asset_reference VARCHAR(160),

    bed_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_bed_code
        UNIQUE (tenant_id, boarding_room_id, bed_code),

    CONSTRAINT ck_gts_boarding_bed_type
        CHECK (
            bed_type IN (
                'STANDARD',
                'BUNK',
                'ACCESSIBLE',
                'MEDICAL',
                'TEMPORARY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_bed_lifecycle
        CHECK (
            bed_status IN (
                'AVAILABLE',
                'ALLOCATED',
                'RESERVED',
                'MAINTENANCE',
                'DAMAGED',
                'UNAVAILABLE',
                'RETIRED'
            )
        ),

    CONSTRAINT ck_gts_boarding_bed_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_staff_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    assignment_reference VARCHAR(100) NOT NULL,

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id),

    boarding_role VARCHAR(40) NOT NULL,

    primary_assignment BOOLEAN NOT NULL DEFAULT FALSE,
    live_in_staff BOOLEAN NOT NULL DEFAULT FALSE,

    effective_from DATE NOT NULL,
    effective_to DATE,

    duty_start_time TIME,
    duty_end_time TIME,

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_staff_assignment_reference
        UNIQUE (tenant_id, assignment_reference),

    CONSTRAINT uq_gts_boarding_staff_scope
        UNIQUE (
            tenant_id,
            boarding_house_id,
            workforce_member_id,
            boarding_role,
            effective_from
        ),

    CONSTRAINT ck_gts_boarding_staff_role
        CHECK (
            boarding_role IN (
                'HOUSE_PARENT',
                'WARDEN',
                'DEPUTY_WARDEN',
                'MATRON',
                'PATRON',
                'RESIDENTIAL_TUTOR',
                'NIGHT_SUPERVISOR',
                'SECURITY_OFFICER',
                'CLEANER',
                'LAUNDRY_ATTENDANT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_staff_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_boarding_staff_times
        CHECK (
            duty_end_time IS NULL
            OR duty_start_time IS NULL
            OR duty_end_time <> duty_start_time
        ),

    CONSTRAINT ck_gts_boarding_staff_lifecycle
        CHECK (
            assignment_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'REVOKED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_staff_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_enrollment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    enrollment_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    boarding_type VARCHAR(30) NOT NULL DEFAULT 'FULL_BOARDING',

    guardian_id UUID
        REFERENCES gts_guardian(id),

    guardian_consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_consent_at TIMESTAMPTZ,

    medical_clearance_required BOOLEAN NOT NULL DEFAULT FALSE,

    medical_clearance_id UUID
        REFERENCES gts_return_to_school_clearance(id),

    effective_from DATE NOT NULL,
    effective_to DATE,

    fee_assignment_id UUID
        REFERENCES gts_student_fee_assignment(id),

    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    enrollment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_enrollment_reference
        UNIQUE (tenant_id, enrollment_reference),

    CONSTRAINT uq_gts_boarding_enrollment_student_period
        UNIQUE (
            tenant_id,
            student_id,
            academic_year_id,
            academic_term_id
        ),

    CONSTRAINT ck_gts_boarding_enrollment_type
        CHECK (
            boarding_type IN (
                'FULL_BOARDING',
                'WEEKLY_BOARDING',
                'TEMPORARY_BOARDING',
                'EMERGENCY_BOARDING',
                'SPECIAL_NEEDS_BOARDING',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_enrollment_consent
        CHECK (
            guardian_consent_recorded = FALSE
            OR guardian_consent_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_enrollment_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_boarding_enrollment_approval
        CHECK (
            enrollment_status NOT IN ('APPROVED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_enrollment_lifecycle
        CHECK (
            enrollment_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'WITHDRAWN',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_enrollment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_bed_allocation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    allocation_reference VARCHAR(100) NOT NULL,

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    boarding_bed_id UUID NOT NULL
        REFERENCES gts_boarding_bed(id),

    allocated_from DATE NOT NULL,
    allocated_to DATE,

    allocation_reason VARCHAR(1000),

    allocated_at TIMESTAMPTZ NOT NULL,
    allocated_by UUID,

    released_at TIMESTAMPTZ,
    released_by UUID,
    release_reason VARCHAR(1000),

    allocation_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_bed_allocation_reference
        UNIQUE (tenant_id, allocation_reference),

    CONSTRAINT ck_gts_bed_allocation_dates
        CHECK (
            allocated_to IS NULL
            OR allocated_to >= allocated_from
        ),

    CONSTRAINT ck_gts_bed_allocation_release
        CHECK (
            allocation_status <> 'RELEASED'
            OR released_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_bed_allocation_lifecycle
        CHECK (
            allocation_status IN (
                'RESERVED',
                'ACTIVE',
                'TRANSFER_PENDING',
                'TRANSFERRED',
                'RELEASED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_bed_allocation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_active_bed_allocation
    ON gts_bed_allocation (
        tenant_id,
        boarding_bed_id
    )
    WHERE allocation_status IN ('RESERVED', 'ACTIVE')
      AND status = 'ACTIVE';

CREATE TABLE gts_boarding_check_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    event_reference VARCHAR(100) NOT NULL,

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    event_type VARCHAR(30) NOT NULL,

    event_at TIMESTAMPTZ NOT NULL,
    event_location VARCHAR(250),

    recorded_by UUID,

    guardian_present BOOLEAN NOT NULL DEFAULT FALSE,

    guardian_id UUID
        REFERENCES gts_guardian(id),

    items_received JSONB NOT NULL DEFAULT '[]'::jsonb,
    condition_notes VARCHAR(2000),

    key_or_access_reference VARCHAR(160),

    event_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_check_event_reference
        UNIQUE (tenant_id, event_reference),

    CONSTRAINT ck_gts_boarding_check_event_type
        CHECK (
            event_type IN (
                'CHECK_IN',
                'CHECK_OUT',
                'TEMPORARY_EXIT',
                'TEMPORARY_RETURN',
                'TERM_OPENING',
                'TERM_CLOSING',
                'EMERGENCY_EVACUATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_check_event_lifecycle
        CHECK (
            event_status IN (
                'PLANNED',
                'IN_PROGRESS',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_check_event_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_roll_call_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    session_reference VARCHAR(100) NOT NULL,

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    roll_call_date DATE NOT NULL,
    roll_call_type VARCHAR(30) NOT NULL,

    scheduled_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    expected_student_count INTEGER NOT NULL DEFAULT 0,
    recorded_student_count INTEGER NOT NULL DEFAULT 0,

    conducted_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    session_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_roll_call_reference
        UNIQUE (tenant_id, session_reference),

    CONSTRAINT uq_gts_boarding_roll_call_scope
        UNIQUE (
            tenant_id,
            boarding_house_id,
            roll_call_date,
            roll_call_type
        ),

    CONSTRAINT ck_gts_boarding_roll_call_type
        CHECK (
            roll_call_type IN (
                'MORNING',
                'EVENING',
                'NIGHT',
                'MEAL',
                'STUDY',
                'EMERGENCY',
                'SPECIAL'
            )
        ),

    CONSTRAINT ck_gts_boarding_roll_call_dates
        CHECK (
            completed_at IS NULL
            OR started_at IS NULL
            OR completed_at >= started_at
        ),

    CONSTRAINT ck_gts_boarding_roll_call_counts
        CHECK (
            expected_student_count >= 0
            AND recorded_student_count >= 0
            AND recorded_student_count <= expected_student_count
        ),

    CONSTRAINT ck_gts_boarding_roll_call_completion
        CHECK (
            session_status <> 'COMPLETED'
            OR completed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_roll_call_lifecycle
        CHECK (
            session_status IN (
                'PLANNED',
                'OPEN',
                'IN_PROGRESS',
                'COMPLETED',
                'REOPENED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_roll_call_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_roll_call_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    roll_call_session_id UUID NOT NULL
        REFERENCES gts_boarding_roll_call_session(id) ON DELETE CASCADE,

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    attendance_status VARCHAR(30) NOT NULL,

    recorded_at TIMESTAMPTZ NOT NULL,
    recorded_by UUID,

    absence_reason VARCHAR(1000),

    approved_leave_request_id UUID,

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_notes VARCHAR(1500),

    notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    notification_request_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_roll_call_record
        UNIQUE (
            tenant_id,
            roll_call_session_id,
            student_id
        ),

    CONSTRAINT ck_gts_boarding_roll_call_attendance
        CHECK (
            attendance_status IN (
                'PRESENT',
                'ABSENT',
                'AUTHORIZED_LEAVE',
                'SICKBAY',
                'OFF_CAMPUS',
                'LATE',
                'MISSING',
                'NOT_REQUIRED'
            )
        ),

    CONSTRAINT ck_gts_boarding_roll_call_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_leave_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    request_reference VARCHAR(100) NOT NULL,

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    leave_type VARCHAR(40) NOT NULL,
    leave_reason VARCHAR(2500) NOT NULL,

    departure_at TIMESTAMPTZ NOT NULL,
    expected_return_at TIMESTAMPTZ NOT NULL,
    actual_return_at TIMESTAMPTZ,

    destination_address VARCHAR(500),

    pickup_person_name VARCHAR(250),
    pickup_person_phone VARCHAR(40),

    pickup_authorization_id UUID
        REFERENCES gts_student_pickup_authorization(id),

    transport_arrangement VARCHAR(500),

    workflow_instance_id UUID,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    departed_at TIMESTAMPTZ,
    released_by UUID,

    returned_at TIMESTAMPTZ,
    received_by UUID,

    request_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_leave_request_reference
        UNIQUE (tenant_id, request_reference),

    CONSTRAINT ck_gts_boarding_leave_type
        CHECK (
            leave_type IN (
                'WEEKEND_LEAVE',
                'HOME_LEAVE',
                'MEDICAL_LEAVE',
                'FAMILY_EVENT',
                'RELIGIOUS_EVENT',
                'SCHOOL_ACTIVITY',
                'EMERGENCY_LEAVE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_leave_dates
        CHECK (
            expected_return_at >= departure_at
            AND (
                actual_return_at IS NULL
                OR actual_return_at >= departure_at
            )
        ),

    CONSTRAINT ck_gts_boarding_leave_approval
        CHECK (
            request_status NOT IN (
                'APPROVED',
                'DEPARTED',
                'RETURNED',
                'OVERDUE'
            )
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_leave_departure
        CHECK (
            request_status NOT IN (
                'DEPARTED',
                'RETURNED',
                'OVERDUE'
            )
            OR departed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_leave_return
        CHECK (
            request_status <> 'RETURNED'
            OR returned_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_leave_lifecycle
        CHECK (
            request_status IN (
                'DRAFT',
                'PENDING',
                'APPROVED',
                'REJECTED',
                'DEPARTED',
                'RETURNED',
                'OVERDUE',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_leave_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

ALTER TABLE gts_boarding_roll_call_record
    ADD CONSTRAINT fk_gts_boarding_roll_call_leave
    FOREIGN KEY (approved_leave_request_id)
    REFERENCES gts_boarding_leave_request(id);

CREATE TABLE gts_boarding_visitor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    visitor_reference VARCHAR(100) NOT NULL,

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    student_id UUID
        REFERENCES gts_student(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    visitor_name VARCHAR(250) NOT NULL,
    visitor_phone VARCHAR(40),

    relationship_to_student VARCHAR(120),

    identity_document_type VARCHAR(40),
    identity_document_reference VARCHAR(160),

    visit_purpose VARCHAR(1500),

    scheduled_at TIMESTAMPTZ,
    checked_in_at TIMESTAMPTZ,
    checked_out_at TIMESTAMPTZ,

    approved_by UUID,
    checked_in_by UUID,
    checked_out_by UUID,

    visit_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_visitor_reference
        UNIQUE (tenant_id, visitor_reference),

    CONSTRAINT ck_gts_boarding_visitor_identity
        CHECK (
            identity_document_type IS NULL
            OR identity_document_type IN (
                'NATIONAL_ID',
                'PASSPORT',
                'DRIVING_PERMIT',
                'SCHOOL_ID',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_visitor_times
        CHECK (
            checked_out_at IS NULL
            OR checked_in_at IS NULL
            OR checked_out_at >= checked_in_at
        ),

    CONSTRAINT ck_gts_boarding_visitor_lifecycle
        CHECK (
            visit_status IN (
                'PLANNED',
                'APPROVED',
                'CHECKED_IN',
                'CHECKED_OUT',
                'REJECTED',
                'CANCELLED',
                'OVERSTAYED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_visitor_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_night_movement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    movement_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    movement_type VARCHAR(40) NOT NULL,

    from_location VARCHAR(250) NOT NULL,
    to_location VARCHAR(250) NOT NULL,

    movement_reason VARCHAR(1500) NOT NULL,

    departed_at TIMESTAMPTZ NOT NULL,
    expected_return_at TIMESTAMPTZ,
    returned_at TIMESTAMPTZ,

    accompanied_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    approved_by UUID,

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    movement_status VARCHAR(30) NOT NULL DEFAULT 'DEPARTED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_night_movement_reference
        UNIQUE (tenant_id, movement_reference),

    CONSTRAINT ck_gts_boarding_night_movement_type
        CHECK (
            movement_type IN (
                'SICKBAY',
                'BATHROOM',
                'WARDEN_OFFICE',
                'EMERGENCY',
                'PRAYER',
                'SECURITY',
                'AUTHORIZED_ACTIVITY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_night_movement_times
        CHECK (
            returned_at IS NULL
            OR returned_at >= departed_at
        ),

    CONSTRAINT ck_gts_boarding_night_movement_lifecycle
        CHECK (
            movement_status IN (
                'DEPARTED',
                'IN_PROGRESS',
                'RETURNED',
                'OVERDUE',
                'ESCALATED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_boarding_night_movement_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_inspection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    inspection_reference VARCHAR(100) NOT NULL,

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    boarding_room_id UUID
        REFERENCES gts_boarding_room(id),

    inspection_type VARCHAR(40) NOT NULL,

    scheduled_date DATE,
    inspected_at TIMESTAMPTZ,

    inspected_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    cleanliness_rating INTEGER,
    safety_rating INTEGER,
    maintenance_rating INTEGER,

    findings VARCHAR(3000),
    corrective_actions VARCHAR(2500),

    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_date DATE,

    workflow_instance_id UUID,

    inspection_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_inspection_reference
        UNIQUE (tenant_id, inspection_reference),

    CONSTRAINT ck_gts_boarding_inspection_type
        CHECK (
            inspection_type IN (
                'DAILY_CLEANLINESS',
                'WEEKLY_HOUSE',
                'SAFETY',
                'FIRE',
                'HEALTH',
                'MAINTENANCE',
                'BEDDING',
                'SECURITY',
                'SPECIAL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_inspection_ratings
        CHECK (
            (
                cleanliness_rating IS NULL
                OR cleanliness_rating BETWEEN 1 AND 5
            )
            AND (
                safety_rating IS NULL
                OR safety_rating BETWEEN 1 AND 5
            )
            AND (
                maintenance_rating IS NULL
                OR maintenance_rating BETWEEN 1 AND 5
            )
        ),

    CONSTRAINT ck_gts_boarding_inspection_completion
        CHECK (
            inspection_status <> 'COMPLETED'
            OR inspected_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_inspection_lifecycle
        CHECK (
            inspection_status IN (
                'PLANNED',
                'IN_PROGRESS',
                'COMPLETED',
                'ACTION_REQUIRED',
                'FOLLOW_UP',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_inspection_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_incident (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    incident_reference VARCHAR(100) NOT NULL,

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    boarding_room_id UUID
        REFERENCES gts_boarding_room(id),

    student_id UUID
        REFERENCES gts_student(id),

    incident_type VARCHAR(40) NOT NULL,

    incident_at TIMESTAMPTZ NOT NULL,
    incident_description VARCHAR(3000) NOT NULL,

    severity VARCHAR(20) NOT NULL,

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    safeguarding_action_id UUID
        REFERENCES gts_safeguarding_action(id),

    emergency_response_required BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,

    workflow_instance_id UUID,

    reported_by UUID,

    resolved_at TIMESTAMPTZ,
    resolved_by UUID,
    resolution_notes VARCHAR(2500),

    incident_status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_incident_reference
        UNIQUE (tenant_id, incident_reference),

    CONSTRAINT ck_gts_boarding_incident_type
        CHECK (
            incident_type IN (
                'MISSING_STUDENT',
                'UNAUTHORIZED_EXIT',
                'LATE_RETURN',
                'BULLYING',
                'FIGHT',
                'THEFT',
                'PROPERTY_DAMAGE',
                'FIRE',
                'MEDICAL_EMERGENCY',
                'SECURITY_INCIDENT',
                'SAFEGUARDING',
                'NOISE',
                'PROHIBITED_ITEM',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_incident_severity
        CHECK (
            severity IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_boarding_incident_resolution
        CHECK (
            incident_status <> 'RESOLVED'
            OR resolved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_incident_lifecycle
        CHECK (
            incident_status IN (
                'REPORTED',
                'TRIAGED',
                'UNDER_INVESTIGATION',
                'ACTION_REQUIRED',
                'REFERRED',
                'RESOLVED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_incident_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_laundry_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    item_code VARCHAR(100) NOT NULL,
    item_type VARCHAR(40) NOT NULL,
    item_description VARCHAR(500),

    quantity INTEGER NOT NULL DEFAULT 1,
    labelled BOOLEAN NOT NULL DEFAULT FALSE,

    item_condition VARCHAR(30) NOT NULL DEFAULT 'GOOD',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_laundry_item
        UNIQUE (
            tenant_id,
            boarding_enrollment_id,
            item_code
        ),

    CONSTRAINT ck_gts_boarding_laundry_item_type
        CHECK (
            item_type IN (
                'UNIFORM',
                'CASUAL_WEAR',
                'BED_SHEET',
                'BLANKET',
                'TOWEL',
                'SHOE',
                'CURTAIN',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_laundry_quantity
        CHECK (quantity > 0),

    CONSTRAINT ck_gts_boarding_laundry_condition
        CHECK (
            item_condition IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED',
                'LOST'
            )
        ),

    CONSTRAINT ck_gts_boarding_laundry_item_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_laundry_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transaction_reference VARCHAR(100) NOT NULL,

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    transaction_type VARCHAR(30) NOT NULL,

    collected_at TIMESTAMPTZ,
    collected_by UUID,

    expected_return_at TIMESTAMPTZ,
    returned_at TIMESTAMPTZ,
    returned_by UUID,

    item_count INTEGER NOT NULL DEFAULT 0,
    missing_item_count INTEGER NOT NULL DEFAULT 0,
    damaged_item_count INTEGER NOT NULL DEFAULT 0,

    notes VARCHAR(1500),

    transaction_status VARCHAR(30) NOT NULL DEFAULT 'COLLECTED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_laundry_transaction_reference
        UNIQUE (tenant_id, transaction_reference),

    CONSTRAINT ck_gts_boarding_laundry_transaction_type
        CHECK (
            transaction_type IN (
                'COLLECTION',
                'RETURN',
                'REWASH',
                'REPAIR',
                'REPLACEMENT',
                'LOSS_REPORT'
            )
        ),

    CONSTRAINT ck_gts_boarding_laundry_counts
        CHECK (
            item_count >= 0
            AND missing_item_count >= 0
            AND damaged_item_count >= 0
            AND missing_item_count + damaged_item_count <= item_count
        ),

    CONSTRAINT ck_gts_boarding_laundry_dates
        CHECK (
            returned_at IS NULL
            OR collected_at IS NULL
            OR returned_at >= collected_at
        ),

    CONSTRAINT ck_gts_boarding_laundry_lifecycle
        CHECK (
            transaction_status IN (
                'PLANNED',
                'COLLECTED',
                'PROCESSING',
                'READY',
                'RETURNED',
                'PARTIALLY_RETURNED',
                'DISPUTED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_boarding_laundry_transaction_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_inventory_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    boarding_room_id UUID
        REFERENCES gts_boarding_room(id),

    item_code VARCHAR(100) NOT NULL,
    item_name VARCHAR(250) NOT NULL,
    item_category VARCHAR(40) NOT NULL,

    quantity INTEGER NOT NULL DEFAULT 1,
    unit_of_measure VARCHAR(40),

    asset_reference VARCHAR(160),

    condition_status VARCHAR(30) NOT NULL DEFAULT 'GOOD',

    last_checked_at TIMESTAMPTZ,
    last_checked_by UUID,

    replacement_cost NUMERIC(18,2),
    currency_code VARCHAR(3),

    inventory_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_inventory_item
        UNIQUE (
            tenant_id,
            boarding_house_id,
            item_code
        ),

    CONSTRAINT ck_gts_boarding_inventory_category
        CHECK (
            item_category IN (
                'BEDDING',
                'FURNITURE',
                'CLEANING',
                'SAFETY',
                'MEDICAL',
                'SPORTS',
                'STUDY',
                'KITCHEN',
                'LAUNDRY',
                'ELECTRICAL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_inventory_quantity
        CHECK (quantity >= 0),

    CONSTRAINT ck_gts_boarding_inventory_condition
        CHECK (
            condition_status IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED',
                'LOST',
                'RETIRED'
            )
        ),

    CONSTRAINT ck_gts_boarding_inventory_cost
        CHECK (
            replacement_cost IS NULL
            OR replacement_cost >= 0
        ),

    CONSTRAINT ck_gts_boarding_inventory_lifecycle
        CHECK (
            inventory_status IN (
                'AVAILABLE',
                'ASSIGNED',
                'IN_USE',
                'MAINTENANCE',
                'MISSING',
                'DAMAGED',
                'RETIRED'
            )
        ),

    CONSTRAINT ck_gts_boarding_inventory_item_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_meal_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    assignment_reference VARCHAR(100) NOT NULL,

    boarding_enrollment_id UUID NOT NULL
        REFERENCES gts_boarding_enrollment(id),

    meal_plan_code VARCHAR(100) NOT NULL,
    meal_plan_name VARCHAR(250),

    breakfast_included BOOLEAN NOT NULL DEFAULT TRUE,
    lunch_included BOOLEAN NOT NULL DEFAULT TRUE,
    supper_included BOOLEAN NOT NULL DEFAULT TRUE,
    snack_included BOOLEAN NOT NULL DEFAULT FALSE,

    dietary_requirements VARCHAR(1500),

    allergy_alert_id UUID
        REFERENCES gts_student_medical_alert(id),

    effective_from DATE NOT NULL,
    effective_to DATE,

    fee_assignment_id UUID
        REFERENCES gts_student_fee_assignment(id),

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_meal_assignment_reference
        UNIQUE (tenant_id, assignment_reference),

    CONSTRAINT ck_gts_boarding_meal_assignment_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_boarding_meal_assignment_lifecycle
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

    CONSTRAINT ck_gts_boarding_meal_assignment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_emergency_register (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    emergency_reference VARCHAR(100) NOT NULL,

    boarding_house_id UUID NOT NULL
        REFERENCES gts_boarding_house(id),

    emergency_type VARCHAR(40) NOT NULL,

    emergency_started_at TIMESTAMPTZ NOT NULL,
    assembly_completed_at TIMESTAMPTZ,
    emergency_ended_at TIMESTAMPTZ,

    expected_student_count INTEGER NOT NULL DEFAULT 0,
    accounted_student_count INTEGER NOT NULL DEFAULT 0,
    missing_student_count INTEGER NOT NULL DEFAULT 0,

    assembly_location VARCHAR(300),

    emergency_lead_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    emergency_services_contacted BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,

    workflow_instance_id UUID,

    emergency_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_emergency_reference
        UNIQUE (tenant_id, emergency_reference),

    CONSTRAINT ck_gts_boarding_emergency_type
        CHECK (
            emergency_type IN (
                'FIRE',
                'MEDICAL',
                'SECURITY',
                'MISSING_STUDENT',
                'WEATHER',
                'STRUCTURAL',
                'INFECTIOUS_DISEASE',
                'EVACUATION_DRILL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_emergency_dates
        CHECK (
            emergency_ended_at IS NULL
            OR emergency_ended_at >= emergency_started_at
        ),

    CONSTRAINT ck_gts_boarding_emergency_counts
        CHECK (
            expected_student_count >= 0
            AND accounted_student_count >= 0
            AND missing_student_count >= 0
            AND accounted_student_count + missing_student_count
                <= expected_student_count
        ),

    CONSTRAINT ck_gts_boarding_emergency_completion
        CHECK (
            emergency_status <> 'COMPLETED'
            OR emergency_ended_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_emergency_lifecycle
        CHECK (
            emergency_status IN (
                'ACTIVE',
                'EVACUATING',
                'ASSEMBLED',
                'ACCOUNTING',
                'ESCALATED',
                'RESOLVED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_boarding_emergency_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_emergency_student (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    emergency_register_id UUID NOT NULL
        REFERENCES gts_boarding_emergency_register(id) ON DELETE CASCADE,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    boarding_enrollment_id UUID
        REFERENCES gts_boarding_enrollment(id),

    accounting_status VARCHAR(30) NOT NULL DEFAULT 'EXPECTED',

    accounted_at TIMESTAMPTZ,
    accounted_by UUID,

    location_found VARCHAR(300),
    notes VARCHAR(1500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_boarding_emergency_student
        UNIQUE (
            tenant_id,
            emergency_register_id,
            student_id
        ),

    CONSTRAINT ck_gts_boarding_emergency_accounting
        CHECK (
            accounting_status IN (
                'EXPECTED',
                'ACCOUNTED_FOR',
                'MISSING',
                'OFF_CAMPUS',
                'SICKBAY',
                'WITH_GUARDIAN',
                'REFERRED',
                'NOT_REQUIRED'
            )
        ),

    CONSTRAINT ck_gts_boarding_emergency_accounted
        CHECK (
            accounting_status <> 'ACCOUNTED_FOR'
            OR accounted_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_boarding_emergency_student_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    notification_reference VARCHAR(100) NOT NULL,

    boarding_enrollment_id UUID
        REFERENCES gts_boarding_enrollment(id),

    student_id UUID
        REFERENCES gts_student(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    notification_type VARCHAR(40) NOT NULL,
    communication_channel VARCHAR(30) NOT NULL,

    notification_request_id UUID,

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

    CONSTRAINT uq_gts_boarding_notification_reference
        UNIQUE (tenant_id, notification_reference),

    CONSTRAINT ck_gts_boarding_notification_type
        CHECK (
            notification_type IN (
                'CHECK_IN_CONFIRMED',
                'CHECK_OUT_CONFIRMED',
                'ROLL_CALL_ABSENCE',
                'LEAVE_APPROVED',
                'LEAVE_REJECTED',
                'LEAVE_OVERDUE',
                'STUDENT_RETURNED',
                'VISITOR_NOTICE',
                'MEDICAL_ALERT',
                'DISCIPLINE_ALERT',
                'EMERGENCY_ALERT',
                'ROOM_TRANSFER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_boarding_notification_channel
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

    CONSTRAINT ck_gts_boarding_notification_lifecycle
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

    CONSTRAINT ck_gts_boarding_notification_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_boarding_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,

    event_type VARCHAR(50) NOT NULL,
    event_description VARCHAR(2000),

    previous_value JSONB,
    new_value JSONB,

    effective_at TIMESTAMPTZ NOT NULL,
    event_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_boarding_history_entity
        CHECK (
            entity_type IN (
                'BOARDING_HOUSE',
                'BOARDING_ROOM',
                'BOARDING_BED',
                'BOARDING_STAFF',
                'BOARDING_ENROLLMENT',
                'BED_ALLOCATION',
                'CHECK_EVENT',
                'ROLL_CALL_SESSION',
                'ROLL_CALL_RECORD',
                'LEAVE_REQUEST',
                'VISITOR',
                'NIGHT_MOVEMENT',
                'INSPECTION',
                'BOARDING_INCIDENT',
                'LAUNDRY_ITEM',
                'LAUNDRY_TRANSACTION',
                'INVENTORY_ITEM',
                'MEAL_ASSIGNMENT',
                'EMERGENCY_REGISTER',
                'EMERGENCY_STUDENT',
                'NOTIFICATION'
            )
        ),

    CONSTRAINT ck_gts_boarding_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'ENROLLED',
                'APPROVED',
                'BED_ALLOCATED',
                'BED_TRANSFERRED',
                'BED_RELEASED',
                'CHECKED_IN',
                'CHECKED_OUT',
                'ROLL_CALL_OPENED',
                'ROLL_CALL_COMPLETED',
                'STUDENT_ABSENT',
                'MISSING_STUDENT',
                'LEAVE_REQUESTED',
                'LEAVE_APPROVED',
                'STUDENT_DEPARTED',
                'STUDENT_RETURNED',
                'VISITOR_CHECKED_IN',
                'VISITOR_CHECKED_OUT',
                'NIGHT_MOVEMENT_RECORDED',
                'INSPECTION_COMPLETED',
                'INCIDENT_REPORTED',
                'INCIDENT_RESOLVED',
                'EMERGENCY_STARTED',
                'EMERGENCY_COMPLETED',
                'NOTIFICATION_SENT',
                'SUSPENDED',
                'COMPLETED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_boarding_house_campus
    ON gts_boarding_house (
        tenant_id,
        campus_id,
        house_type,
        house_status
    );

CREATE INDEX ix_gts_boarding_room_house
    ON gts_boarding_room (
        tenant_id,
        boarding_house_id,
        room_status
    );

CREATE INDEX ix_gts_boarding_bed_room
    ON gts_boarding_bed (
        tenant_id,
        boarding_room_id,
        bed_status
    );

CREATE INDEX ix_gts_boarding_staff_house
    ON gts_boarding_staff_assignment (
        tenant_id,
        boarding_house_id,
        boarding_role,
        assignment_status
    );

CREATE INDEX ix_gts_boarding_enrollment_student
    ON gts_boarding_enrollment (
        tenant_id,
        student_id,
        academic_year_id,
        academic_term_id,
        enrollment_status
    );

CREATE INDEX ix_gts_boarding_enrollment_house
    ON gts_boarding_enrollment (
        tenant_id,
        boarding_house_id,
        enrollment_status
    );

CREATE INDEX ix_gts_bed_allocation_enrollment
    ON gts_bed_allocation (
        tenant_id,
        boarding_enrollment_id,
        allocation_status
    );

CREATE INDEX ix_gts_boarding_check_event_enrollment
    ON gts_boarding_check_event (
        tenant_id,
        boarding_enrollment_id,
        event_at,
        event_type
    );

CREATE INDEX ix_gts_boarding_roll_call_house
    ON gts_boarding_roll_call_session (
        tenant_id,
        boarding_house_id,
        roll_call_date,
        session_status
    );

CREATE INDEX ix_gts_boarding_roll_call_student
    ON gts_boarding_roll_call_record (
        tenant_id,
        student_id,
        attendance_status
    );

CREATE INDEX ix_gts_boarding_leave_student
    ON gts_boarding_leave_request (
        tenant_id,
        student_id,
        departure_at,
        request_status
    );

CREATE INDEX ix_gts_boarding_visitor_house
    ON gts_boarding_visitor (
        tenant_id,
        boarding_house_id,
        checked_in_at,
        visit_status
    );

CREATE INDEX ix_gts_boarding_night_movement_student
    ON gts_boarding_night_movement (
        tenant_id,
        student_id,
        departed_at,
        movement_status
    );

CREATE INDEX ix_gts_boarding_inspection_house
    ON gts_boarding_inspection (
        tenant_id,
        boarding_house_id,
        scheduled_date,
        inspection_status
    );

CREATE INDEX ix_gts_boarding_incident_house
    ON gts_boarding_incident (
        tenant_id,
        boarding_house_id,
        incident_at,
        severity,
        incident_status
    );

CREATE INDEX ix_gts_boarding_laundry_item_enrollment
    ON gts_boarding_laundry_item (
        tenant_id,
        boarding_enrollment_id,
        item_condition
    );

CREATE INDEX ix_gts_boarding_laundry_transaction
    ON gts_boarding_laundry_transaction (
        tenant_id,
        boarding_enrollment_id,
        transaction_status
    );

CREATE INDEX ix_gts_boarding_inventory_house
    ON gts_boarding_inventory_item (
        tenant_id,
        boarding_house_id,
        item_category,
        inventory_status
    );

CREATE INDEX ix_gts_boarding_meal_enrollment
    ON gts_boarding_meal_assignment (
        tenant_id,
        boarding_enrollment_id,
        assignment_status
    );

CREATE INDEX ix_gts_boarding_emergency_house
    ON gts_boarding_emergency_register (
        tenant_id,
        boarding_house_id,
        emergency_started_at,
        emergency_status
    );

CREATE INDEX ix_gts_boarding_emergency_student_record
    ON gts_boarding_emergency_student (
        tenant_id,
        emergency_register_id,
        accounting_status
    );

CREATE INDEX ix_gts_boarding_notification_student
    ON gts_boarding_notification (
        tenant_id,
        student_id,
        notification_status
    );

CREATE INDEX ix_gts_boarding_history_entity
    ON gts_boarding_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '049',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
