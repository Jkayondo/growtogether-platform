CREATE TABLE gts_transport_provider (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    provider_code VARCHAR(80) NOT NULL,
    provider_name VARCHAR(250) NOT NULL,
    description VARCHAR(1200),

    provider_type VARCHAR(40) NOT NULL,
    registration_number VARCHAR(120),
    tax_reference VARCHAR(120),

    primary_contact_name VARCHAR(200),
    primary_phone VARCHAR(40),
    primary_email VARCHAR(200),

    physical_address VARCHAR(500),

    contract_document_id UUID,
    contract_start_date DATE,
    contract_end_date DATE,

    external_provider_reference VARCHAR(160),

    provider_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_provider_code
        UNIQUE (tenant_id, provider_code),

    CONSTRAINT ck_gts_transport_provider_type
        CHECK (
            provider_type IN (
                'SCHOOL_OWNED',
                'CONTRACTED_COMPANY',
                'INDIVIDUAL_OPERATOR',
                'PUBLIC_PROVIDER',
                'COMMUNITY_PROVIDER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_provider_dates
        CHECK (
            contract_end_date IS NULL
            OR contract_start_date IS NULL
            OR contract_end_date >= contract_start_date
        ),

    CONSTRAINT ck_gts_transport_provider_lifecycle
        CHECK (
            provider_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'CONTRACT_EXPIRED',
                'TERMINATED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_provider_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_vehicle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transport_provider_id UUID
        REFERENCES gts_transport_provider(id),

    campus_id UUID
        REFERENCES gts_campus(id),

    vehicle_code VARCHAR(80) NOT NULL,
    registration_number VARCHAR(80) NOT NULL,

    vehicle_type VARCHAR(40) NOT NULL,
    make VARCHAR(120),
    model VARCHAR(120),
    manufacture_year INTEGER,

    seating_capacity INTEGER NOT NULL,
    standing_capacity INTEGER NOT NULL DEFAULT 0,

    wheelchair_accessible BOOLEAN NOT NULL DEFAULT FALSE,
    gps_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    gps_device_reference VARCHAR(160),

    ownership_type VARCHAR(30) NOT NULL,
    acquisition_date DATE,

    current_odometer NUMERIC(14,2),
    fuel_type VARCHAR(30),

    vehicle_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_vehicle_code
        UNIQUE (tenant_id, vehicle_code),

    CONSTRAINT uq_gts_transport_registration
        UNIQUE (tenant_id, registration_number),

    CONSTRAINT ck_gts_transport_vehicle_type
        CHECK (
            vehicle_type IN (
                'BUS',
                'MINIBUS',
                'VAN',
                'CAR',
                'MOTORCYCLE',
                'BOAT',
                'BICYCLE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_year
        CHECK (
            manufacture_year IS NULL
            OR manufacture_year >= 1900
        ),

    CONSTRAINT ck_gts_transport_vehicle_capacity
        CHECK (
            seating_capacity > 0
            AND standing_capacity >= 0
        ),

    CONSTRAINT ck_gts_transport_vehicle_ownership
        CHECK (
            ownership_type IN (
                'SCHOOL_OWNED',
                'LEASED',
                'HIRED',
                'PROVIDER_OWNED',
                'DONATED',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_odometer
        CHECK (
            current_odometer IS NULL
            OR current_odometer >= 0
        ),

    CONSTRAINT ck_gts_transport_vehicle_fuel
        CHECK (
            fuel_type IS NULL
            OR fuel_type IN (
                'PETROL',
                'DIESEL',
                'ELECTRIC',
                'HYBRID',
                'GAS',
                'HUMAN_POWERED',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_lifecycle
        CHECK (
            vehicle_status IN (
                'PLANNED',
                'ACTIVE',
                'MAINTENANCE',
                'OUT_OF_SERVICE',
                'SUSPENDED',
                'RETIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_vehicle_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transport_vehicle_id UUID NOT NULL
        REFERENCES gts_transport_vehicle(id) ON DELETE CASCADE,

    document_type VARCHAR(50) NOT NULL,
    document_reference VARCHAR(160),

    issued_at DATE,
    expires_at DATE,

    issuing_authority VARCHAR(250),
    eds_document_id UUID,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    document_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_vehicle_document
        UNIQUE (
            tenant_id,
            transport_vehicle_id,
            document_type,
            document_reference
        ),

    CONSTRAINT ck_gts_transport_vehicle_document_type
        CHECK (
            document_type IN (
                'REGISTRATION',
                'INSURANCE',
                'ROADWORTHINESS',
                'INSPECTION',
                'OPERATING_LICENCE',
                'OWNERSHIP',
                'LEASE_AGREEMENT',
                'GPS_CERTIFICATE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_document_dates
        CHECK (
            expires_at IS NULL
            OR issued_at IS NULL
            OR expires_at >= issued_at
        ),

    CONSTRAINT ck_gts_transport_vehicle_document_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_document_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_transport_vehicle_document_lifecycle
        CHECK (
            document_status IN (
                'ACTIVE',
                'EXPIRING',
                'EXPIRED',
                'SUSPENDED',
                'REVOKED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_vehicle_document_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_staff_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    assignment_reference VARCHAR(100) NOT NULL,

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id),

    transport_provider_id UUID
        REFERENCES gts_transport_provider(id),

    transport_vehicle_id UUID
        REFERENCES gts_transport_vehicle(id),

    transport_role VARCHAR(30) NOT NULL,

    licence_number VARCHAR(120),
    licence_class VARCHAR(80),
    licence_expiry_date DATE,

    effective_from DATE NOT NULL,
    effective_to DATE,

    primary_assignment BOOLEAN NOT NULL DEFAULT FALSE,

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_staff_assignment_reference
        UNIQUE (tenant_id, assignment_reference),

    CONSTRAINT ck_gts_transport_staff_role
        CHECK (
            transport_role IN (
                'DRIVER',
                'ASSISTANT_DRIVER',
                'CONDUCTOR',
                'ATTENDANT',
                'ROUTE_SUPERVISOR',
                'TRANSPORT_COORDINATOR',
                'MECHANIC',
                'DISPATCHER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_staff_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_transport_staff_lifecycle
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

    CONSTRAINT ck_gts_transport_staff_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_route (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    route_code VARCHAR(80) NOT NULL,
    route_name VARCHAR(250) NOT NULL,
    description VARCHAR(1200),

    route_type VARCHAR(30) NOT NULL DEFAULT 'DAILY_SCHOOL',

    origin_name VARCHAR(250) NOT NULL,
    destination_name VARCHAR(250) NOT NULL,

    estimated_distance_km NUMERIC(10,2),
    estimated_duration_minutes INTEGER,

    morning_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    evening_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    fee_item_id UUID
        REFERENCES gts_fee_item(id),

    route_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_route_code
        UNIQUE (tenant_id, route_code),

    CONSTRAINT ck_gts_transport_route_type
        CHECK (
            route_type IN (
                'DAILY_SCHOOL',
                'BOARDING',
                'FIELD_TRIP',
                'SPORTS',
                'STAFF',
                'SPECIAL_NEEDS',
                'EMERGENCY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_route_measurements
        CHECK (
            (
                estimated_distance_km IS NULL
                OR estimated_distance_km >= 0
            )
            AND (
                estimated_duration_minutes IS NULL
                OR estimated_duration_minutes > 0
            )
        ),

    CONSTRAINT ck_gts_transport_route_lifecycle
        CHECK (
            route_status IN (
                'DRAFT',
                'ACTIVE',
                'SUSPENDED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_route_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_stop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    route_id UUID NOT NULL
        REFERENCES gts_transport_route(id) ON DELETE CASCADE,

    stop_code VARCHAR(80) NOT NULL,
    stop_name VARCHAR(250) NOT NULL,

    stop_description VARCHAR(500),
    sequence_number INTEGER NOT NULL,

    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),

    morning_pickup_time TIME,
    evening_dropoff_time TIME,

    estimated_wait_minutes INTEGER NOT NULL DEFAULT 0,

    safe_pickup_location BOOLEAN NOT NULL DEFAULT TRUE,
    guardian_presence_required BOOLEAN NOT NULL DEFAULT FALSE,

    stop_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_stop_code
        UNIQUE (tenant_id, route_id, stop_code),

    CONSTRAINT uq_gts_transport_stop_sequence
        UNIQUE (tenant_id, route_id, sequence_number),

    CONSTRAINT ck_gts_transport_stop_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_transport_stop_coordinates
        CHECK (
            (
                latitude IS NULL
                OR latitude BETWEEN -90 AND 90
            )
            AND (
                longitude IS NULL
                OR longitude BETWEEN -180 AND 180
            )
        ),

    CONSTRAINT ck_gts_transport_stop_wait
        CHECK (estimated_wait_minutes >= 0),

    CONSTRAINT ck_gts_transport_stop_lifecycle
        CHECK (
            stop_status IN (
                'ACTIVE',
                'TEMPORARILY_CLOSED',
                'UNSAFE',
                'RELOCATED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_stop_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    schedule_reference VARCHAR(100) NOT NULL,

    route_id UUID NOT NULL
        REFERENCES gts_transport_route(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    journey_direction VARCHAR(20) NOT NULL,

    monday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tuesday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    wednesday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    thursday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    friday_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    saturday_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    sunday_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    planned_departure_time TIME NOT NULL,
    planned_arrival_time TIME NOT NULL,

    effective_from DATE NOT NULL,
    effective_to DATE,

    schedule_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_schedule_reference
        UNIQUE (tenant_id, schedule_reference),

    CONSTRAINT ck_gts_transport_schedule_direction
        CHECK (
            journey_direction IN (
                'TO_SCHOOL',
                'FROM_SCHOOL',
                'ROUND_TRIP',
                'OUTBOUND',
                'RETURN'
            )
        ),

    CONSTRAINT ck_gts_transport_schedule_times
        CHECK (
            planned_arrival_time <> planned_departure_time
        ),

    CONSTRAINT ck_gts_transport_schedule_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_transport_schedule_lifecycle
        CHECK (
            schedule_status IN (
                'DRAFT',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_schedule_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_transport_registration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    registration_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    route_id UUID NOT NULL
        REFERENCES gts_transport_route(id),

    morning_stop_id UUID
        REFERENCES gts_transport_stop(id),

    evening_stop_id UUID
        REFERENCES gts_transport_stop(id),

    morning_transport_required BOOLEAN NOT NULL DEFAULT TRUE,
    evening_transport_required BOOLEAN NOT NULL DEFAULT TRUE,

    special_transport_needs VARCHAR(1500),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    guardian_consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_consent_at TIMESTAMPTZ,

    effective_from DATE NOT NULL,
    effective_to DATE,

    fee_assignment_id UUID
        REFERENCES gts_student_fee_assignment(id),

    workflow_instance_id UUID,

    registration_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_transport_registration_reference
        UNIQUE (tenant_id, registration_reference),

    CONSTRAINT uq_gts_student_transport_route
        UNIQUE (
            tenant_id,
            student_id,
            route_id,
            effective_from
        ),

    CONSTRAINT ck_gts_student_transport_consent
        CHECK (
            guardian_consent_recorded = FALSE
            OR guardian_consent_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_transport_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_student_transport_lifecycle
        CHECK (
            registration_status IN (
                'PENDING',
                'APPROVED',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_transport_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_journey (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    journey_reference VARCHAR(100) NOT NULL,

    transport_schedule_id UUID
        REFERENCES gts_transport_schedule(id),

    route_id UUID NOT NULL
        REFERENCES gts_transport_route(id),

    transport_vehicle_id UUID NOT NULL
        REFERENCES gts_transport_vehicle(id),

    journey_date DATE NOT NULL,
    journey_direction VARCHAR(20) NOT NULL,

    planned_departure_at TIMESTAMPTZ,
    actual_departure_at TIMESTAMPTZ,

    planned_arrival_at TIMESTAMPTZ,
    actual_arrival_at TIMESTAMPTZ,

    starting_odometer NUMERIC(14,2),
    ending_odometer NUMERIC(14,2),

    expected_student_count INTEGER NOT NULL DEFAULT 0,
    boarded_student_count INTEGER NOT NULL DEFAULT 0,
    alighted_student_count INTEGER NOT NULL DEFAULT 0,

    journey_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    cancellation_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_journey_reference
        UNIQUE (tenant_id, journey_reference),

    CONSTRAINT ck_gts_transport_journey_direction
        CHECK (
            journey_direction IN (
                'TO_SCHOOL',
                'FROM_SCHOOL',
                'OUTBOUND',
                'RETURN'
            )
        ),

    CONSTRAINT ck_gts_transport_journey_times
        CHECK (
            actual_arrival_at IS NULL
            OR actual_departure_at IS NULL
            OR actual_arrival_at >= actual_departure_at
        ),

    CONSTRAINT ck_gts_transport_journey_odometer
        CHECK (
            (starting_odometer IS NULL OR starting_odometer >= 0)
            AND (ending_odometer IS NULL OR ending_odometer >= 0)
            AND (
                ending_odometer IS NULL
                OR starting_odometer IS NULL
                OR ending_odometer >= starting_odometer
            )
        ),

    CONSTRAINT ck_gts_transport_journey_counts
        CHECK (
            expected_student_count >= 0
            AND boarded_student_count >= 0
            AND alighted_student_count >= 0
        ),

    CONSTRAINT ck_gts_transport_journey_lifecycle
        CHECK (
            journey_status IN (
                'PLANNED',
                'BOARDING',
                'DEPARTED',
                'IN_PROGRESS',
                'DELAYED',
                'ARRIVED',
                'COMPLETED',
                'CANCELLED',
                'EMERGENCY_STOP',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_transport_journey_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_journey_staff (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transport_journey_id UUID NOT NULL
        REFERENCES gts_transport_journey(id) ON DELETE CASCADE,

    transport_staff_assignment_id UUID NOT NULL
        REFERENCES gts_transport_staff_assignment(id),

    journey_role VARCHAR(30) NOT NULL,

    checked_in_at TIMESTAMPTZ,
    checked_out_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_journey_staff
        UNIQUE (
            tenant_id,
            transport_journey_id,
            transport_staff_assignment_id
        ),

    CONSTRAINT ck_gts_transport_journey_staff_role
        CHECK (
            journey_role IN (
                'DRIVER',
                'ASSISTANT_DRIVER',
                'CONDUCTOR',
                'ATTENDANT',
                'SUPERVISOR',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_journey_staff_times
        CHECK (
            checked_out_at IS NULL
            OR checked_in_at IS NULL
            OR checked_out_at >= checked_in_at
        ),

    CONSTRAINT ck_gts_transport_journey_staff_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_journey_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transport_journey_id UUID NOT NULL
        REFERENCES gts_transport_journey(id) ON DELETE CASCADE,

    student_transport_registration_id UUID
        REFERENCES gts_student_transport_registration(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    expected_boarding_stop_id UUID
        REFERENCES gts_transport_stop(id),

    expected_alighting_stop_id UUID
        REFERENCES gts_transport_stop(id),

    actual_boarding_stop_id UUID
        REFERENCES gts_transport_stop(id),

    actual_alighting_stop_id UUID
        REFERENCES gts_transport_stop(id),

    boarding_status VARCHAR(30) NOT NULL DEFAULT 'EXPECTED',

    boarded_at TIMESTAMPTZ,
    boarded_by UUID,

    alighted_at TIMESTAMPTZ,
    alighted_by UUID,

    boarding_source VARCHAR(30),
    boarding_reference VARCHAR(160),

    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    boarding_notification_request_id UUID,
    alighting_notification_request_id UUID,

    exception_reason VARCHAR(1500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_journey_record
        UNIQUE (tenant_id, transport_journey_id, student_id),

    CONSTRAINT ck_gts_student_journey_boarding_status
        CHECK (
            boarding_status IN (
                'EXPECTED',
                'BOARDED',
                'ALIGHTED',
                'ABSENT',
                'MISSED_PICKUP',
                'WRONG_STOP',
                'PICKED_UP_BY_GUARDIAN',
                'TRANSFERRED_VEHICLE',
                'EMERGENCY_REMOVAL',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_student_journey_times
        CHECK (
            alighted_at IS NULL
            OR boarded_at IS NULL
            OR alighted_at >= boarded_at
        ),

    CONSTRAINT ck_gts_student_journey_source
        CHECK (
            boarding_source IS NULL
            OR boarding_source IN (
                'MANUAL',
                'RFID',
                'QR_CODE',
                'BIOMETRIC',
                'MOBILE',
                'GPS',
                'SYSTEM'
            )
        ),

    CONSTRAINT ck_gts_student_journey_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_learner_movement_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    movement_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    movement_type VARCHAR(40) NOT NULL,

    from_campus_id UUID
        REFERENCES gts_campus(id),

    to_campus_id UUID
        REFERENCES gts_campus(id),

    from_location VARCHAR(250),
    to_location VARCHAR(250),

    movement_reason VARCHAR(1500),

    departed_at TIMESTAMPTZ,
    arrived_at TIMESTAMPTZ,

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_consent_required BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_consent_at TIMESTAMPTZ,

    transport_journey_id UUID
        REFERENCES gts_transport_journey(id),

    workflow_instance_id UUID,

    movement_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_learner_movement_reference
        UNIQUE (tenant_id, movement_reference),

    CONSTRAINT ck_gts_learner_movement_type
        CHECK (
            movement_type IN (
                'CAMPUS_TRANSFER',
                'FIELD_TRIP',
                'SPORTS_EVENT',
                'MEDICAL_REFERRAL',
                'HOME_LEAVE',
                'BOARDING_MOVEMENT',
                'EMERGENCY_EVACUATION',
                'AUTHORIZED_PICKUP',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_learner_movement_times
        CHECK (
            arrived_at IS NULL
            OR departed_at IS NULL
            OR arrived_at >= departed_at
        ),

    CONSTRAINT ck_gts_learner_movement_consent
        CHECK (
            guardian_consent_recorded = FALSE
            OR guardian_consent_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_learner_movement_lifecycle
        CHECK (
            movement_status IN (
                'PLANNED',
                'PENDING_APPROVAL',
                'APPROVED',
                'DEPARTED',
                'IN_PROGRESS',
                'ARRIVED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_learner_movement_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_pickup_verification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    verification_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_journey_record_id UUID
        REFERENCES gts_student_journey_record(id),

    pickup_authorization_id UUID
        REFERENCES gts_student_pickup_authorization(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    pickup_person_name VARCHAR(250) NOT NULL,
    pickup_person_phone VARCHAR(40),

    identity_document_type VARCHAR(40),
    identity_document_reference VARCHAR(160),

    verification_method VARCHAR(30) NOT NULL,

    verification_code_hash VARCHAR(255),

    verified_at TIMESTAMPTZ,
    verified_by UUID,

    pickup_completed_at TIMESTAMPTZ,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_pickup_verification_reference
        UNIQUE (tenant_id, verification_reference),

    CONSTRAINT ck_gts_transport_pickup_identity_type
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

    CONSTRAINT ck_gts_transport_pickup_method
        CHECK (
            verification_method IN (
                'PIN',
                'QR_CODE',
                'GUARDIAN_CONFIRMATION',
                'PHOTO_MATCH',
                'IDENTITY_DOCUMENT',
                'BIOMETRIC',
                'MANUAL_APPROVAL'
            )
        ),

    CONSTRAINT ck_gts_transport_pickup_verified
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_transport_pickup_lifecycle
        CHECK (
            verification_status IN (
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'EXPIRED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_transport_pickup_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_location_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transport_journey_id UUID NOT NULL
        REFERENCES gts_transport_journey(id) ON DELETE CASCADE,

    event_type VARCHAR(40) NOT NULL,

    recorded_at TIMESTAMPTZ NOT NULL,

    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),

    speed_kph NUMERIC(8,2),
    heading_degrees NUMERIC(6,2),

    transport_stop_id UUID
        REFERENCES gts_transport_stop(id),

    source_type VARCHAR(30) NOT NULL DEFAULT 'GPS',
    source_reference VARCHAR(160),

    event_description VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_transport_location_event_type
        CHECK (
            event_type IN (
                'JOURNEY_STARTED',
                'LOCATION_UPDATE',
                'STOP_ARRIVAL',
                'STOP_DEPARTURE',
                'DELAY_DETECTED',
                'ROUTE_DEVIATION',
                'SPEED_ALERT',
                'UNSCHEDULED_STOP',
                'EMERGENCY_STOP',
                'JOURNEY_COMPLETED',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_location_coordinates
        CHECK (
            (
                latitude IS NULL
                OR latitude BETWEEN -90 AND 90
            )
            AND (
                longitude IS NULL
                OR longitude BETWEEN -180 AND 180
            )
        ),

    CONSTRAINT ck_gts_transport_location_values
        CHECK (
            (speed_kph IS NULL OR speed_kph >= 0)
            AND (
                heading_degrees IS NULL
                OR heading_degrees BETWEEN 0 AND 360
            )
        ),

    CONSTRAINT ck_gts_transport_location_source
        CHECK (
            source_type IN (
                'GPS',
                'MOBILE',
                'MANUAL',
                'TELEMATICS',
                'PROVIDER_API',
                'SYSTEM'
            )
        ),

    CONSTRAINT ck_gts_transport_location_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_substitution (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    substitution_reference VARCHAR(100) NOT NULL,

    transport_journey_id UUID NOT NULL
        REFERENCES gts_transport_journey(id),

    substitution_type VARCHAR(30) NOT NULL,

    original_vehicle_id UUID
        REFERENCES gts_transport_vehicle(id),

    replacement_vehicle_id UUID
        REFERENCES gts_transport_vehicle(id),

    original_staff_assignment_id UUID
        REFERENCES gts_transport_staff_assignment(id),

    replacement_staff_assignment_id UUID
        REFERENCES gts_transport_staff_assignment(id),

    substitution_reason VARCHAR(1500) NOT NULL,

    effective_at TIMESTAMPTZ NOT NULL,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    notification_request_id UUID,

    substitution_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_transport_substitution_reference
        UNIQUE (tenant_id, substitution_reference),

    CONSTRAINT ck_gts_transport_substitution_type
        CHECK (
            substitution_type IN (
                'VEHICLE',
                'DRIVER',
                'ATTENDANT',
                'ROUTE',
                'COMBINED'
            )
        ),

    CONSTRAINT ck_gts_transport_substitution_approval
        CHECK (
            substitution_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_transport_substitution_lifecycle
        CHECK (
            substitution_status IN (
                'PENDING',
                'APPROVED',
                'APPLIED',
                'REJECTED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_transport_substitution_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_incident (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    incident_reference VARCHAR(100) NOT NULL,

    transport_journey_id UUID
        REFERENCES gts_transport_journey(id),

    transport_vehicle_id UUID
        REFERENCES gts_transport_vehicle(id),

    incident_type VARCHAR(40) NOT NULL,

    incident_date DATE NOT NULL,
    incident_time TIME,

    location_description VARCHAR(500),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),

    severity VARCHAR(20) NOT NULL,
    incident_description VARCHAR(3000) NOT NULL,

    injury_reported BOOLEAN NOT NULL DEFAULT FALSE,
    property_damage_reported BOOLEAN NOT NULL DEFAULT FALSE,

    emergency_services_contacted BOOLEAN NOT NULL DEFAULT FALSE,
    police_contacted BOOLEAN NOT NULL DEFAULT FALSE,

    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    authority_notification_required BOOLEAN NOT NULL DEFAULT FALSE,

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    health_injury_incident_id UUID
        REFERENCES gts_health_injury_incident(id),

    workflow_instance_id UUID,

    reported_at TIMESTAMPTZ NOT NULL,
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

    CONSTRAINT uq_gts_transport_incident_reference
        UNIQUE (tenant_id, incident_reference),

    CONSTRAINT ck_gts_transport_incident_type
        CHECK (
            incident_type IN (
                'ACCIDENT',
                'BREAKDOWN',
                'DELAY',
                'MISSED_PICKUP',
                'WRONG_DROPOFF',
                'ROUTE_DEVIATION',
                'UNAUTHORIZED_PASSENGER',
                'STUDENT_MISSING',
                'MEDICAL_EMERGENCY',
                'BEHAVIOUR_INCIDENT',
                'SECURITY_INCIDENT',
                'VEHICLE_DAMAGE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_incident_severity
        CHECK (
            severity IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_transport_incident_coordinates
        CHECK (
            (
                latitude IS NULL
                OR latitude BETWEEN -90 AND 90
            )
            AND (
                longitude IS NULL
                OR longitude BETWEEN -180 AND 180
            )
        ),

    CONSTRAINT ck_gts_transport_incident_resolution
        CHECK (
            incident_status <> 'RESOLVED'
            OR resolved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_transport_incident_lifecycle
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

    CONSTRAINT ck_gts_transport_incident_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    notification_reference VARCHAR(100) NOT NULL,

    transport_journey_id UUID
        REFERENCES gts_transport_journey(id),

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

    CONSTRAINT uq_gts_transport_notification_reference
        UNIQUE (tenant_id, notification_reference),

    CONSTRAINT ck_gts_transport_notification_type
        CHECK (
            notification_type IN (
                'VEHICLE_DEPARTED',
                'STUDENT_BOARDED',
                'STUDENT_ALIGHTED',
                'VEHICLE_DELAYED',
                'MISSED_PICKUP',
                'ROUTE_CHANGED',
                'VEHICLE_CHANGED',
                'DRIVER_CHANGED',
                'INCIDENT_ALERT',
                'EMERGENCY_ALERT',
                'PICKUP_VERIFICATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_transport_notification_channel
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

    CONSTRAINT ck_gts_transport_notification_lifecycle
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

    CONSTRAINT ck_gts_transport_notification_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_transport_history (
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

    CONSTRAINT ck_gts_transport_history_entity
        CHECK (
            entity_type IN (
                'TRANSPORT_PROVIDER',
                'VEHICLE',
                'VEHICLE_DOCUMENT',
                'TRANSPORT_STAFF',
                'ROUTE',
                'STOP',
                'SCHEDULE',
                'STUDENT_REGISTRATION',
                'JOURNEY',
                'JOURNEY_STAFF',
                'STUDENT_JOURNEY',
                'LEARNER_MOVEMENT',
                'PICKUP_VERIFICATION',
                'LOCATION_EVENT',
                'SUBSTITUTION',
                'TRANSPORT_INCIDENT',
                'TRANSPORT_NOTIFICATION'
            )
        ),

    CONSTRAINT ck_gts_transport_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'APPROVED',
                'ASSIGNED',
                'REGISTERED',
                'JOURNEY_PLANNED',
                'JOURNEY_STARTED',
                'STUDENT_BOARDED',
                'STUDENT_ALIGHTED',
                'MISSED_PICKUP',
                'DELAY_RECORDED',
                'ROUTE_DEVIATION',
                'VEHICLE_SUBSTITUTED',
                'DRIVER_SUBSTITUTED',
                'PICKUP_VERIFIED',
                'INCIDENT_REPORTED',
                'INCIDENT_RESOLVED',
                'NOTIFICATION_SENT',
                'JOURNEY_COMPLETED',
                'SUSPENDED',
                'CANCELLED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_transport_provider_status
    ON gts_transport_provider (
        tenant_id,
        provider_type,
        provider_status
    );

CREATE INDEX ix_gts_transport_vehicle_provider
    ON gts_transport_vehicle (
        tenant_id,
        transport_provider_id,
        vehicle_status
    );

CREATE INDEX ix_gts_transport_vehicle_document_expiry
    ON gts_transport_vehicle_document (
        tenant_id,
        transport_vehicle_id,
        expires_at,
        document_status
    );

CREATE INDEX ix_gts_transport_staff_workforce
    ON gts_transport_staff_assignment (
        tenant_id,
        workforce_member_id,
        transport_role,
        assignment_status
    );

CREATE INDEX ix_gts_transport_route_campus
    ON gts_transport_route (
        tenant_id,
        campus_id,
        route_status
    );

CREATE INDEX ix_gts_transport_stop_route
    ON gts_transport_stop (
        tenant_id,
        route_id,
        sequence_number
    );

CREATE INDEX ix_gts_transport_schedule_route
    ON gts_transport_schedule (
        tenant_id,
        route_id,
        academic_year_id,
        academic_term_id,
        schedule_status
    );

CREATE INDEX ix_gts_student_transport_student
    ON gts_student_transport_registration (
        tenant_id,
        student_id,
        registration_status
    );

CREATE INDEX ix_gts_transport_journey_date
    ON gts_transport_journey (
        tenant_id,
        journey_date,
        route_id,
        journey_status
    );

CREATE INDEX ix_gts_transport_journey_vehicle
    ON gts_transport_journey (
        tenant_id,
        transport_vehicle_id,
        journey_date,
        journey_status
    );

CREATE INDEX ix_gts_student_journey_student
    ON gts_student_journey_record (
        tenant_id,
        student_id,
        boarding_status
    );

CREATE INDEX ix_gts_learner_movement_student
    ON gts_learner_movement_record (
        tenant_id,
        student_id,
        movement_status
    );

CREATE INDEX ix_gts_transport_pickup_student
    ON gts_transport_pickup_verification (
        tenant_id,
        student_id,
        verification_status
    );

CREATE INDEX ix_gts_transport_location_journey
    ON gts_transport_location_event (
        tenant_id,
        transport_journey_id,
        recorded_at
    );

CREATE INDEX ix_gts_transport_substitution_journey
    ON gts_transport_substitution (
        tenant_id,
        transport_journey_id,
        substitution_status
    );

CREATE INDEX ix_gts_transport_incident_date
    ON gts_transport_incident (
        tenant_id,
        incident_date,
        severity,
        incident_status
    );

CREATE INDEX ix_gts_transport_notification_journey
    ON gts_transport_notification (
        tenant_id,
        transport_journey_id,
        notification_status
    );

CREATE INDEX ix_gts_transport_history_entity
    ON gts_transport_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '047',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
