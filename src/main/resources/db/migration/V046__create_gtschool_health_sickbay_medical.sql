CREATE TABLE gts_health_facility (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    facility_code VARCHAR(80) NOT NULL,
    facility_name VARCHAR(200) NOT NULL,
    description VARCHAR(1200),

    facility_type VARCHAR(40) NOT NULL,
    location_description VARCHAR(300),

    bed_capacity INTEGER,
    isolation_capacity INTEGER,

    emergency_phone VARCHAR(40),
    external_referral_facility VARCHAR(250),

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    facility_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_facility_code
        UNIQUE (tenant_id, campus_id, facility_code),

    CONSTRAINT ck_gts_health_facility_type
        CHECK (
            facility_type IN (
                'SICKBAY',
                'SCHOOL_CLINIC',
                'FIRST_AID_POINT',
                'ISOLATION_ROOM',
                'COUNSELLING_ROOM',
                'MOBILE_HEALTH_POINT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_facility_capacity
        CHECK (
            (bed_capacity IS NULL OR bed_capacity >= 0)
            AND (
                isolation_capacity IS NULL
                OR isolation_capacity >= 0
            )
        ),

    CONSTRAINT ck_gts_health_facility_lifecycle
        CHECK (
            facility_status IN (
                'PLANNED',
                'ACTIVE',
                'TEMPORARILY_CLOSED',
                'MAINTENANCE',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_health_facility_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_medical_alert (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    medical_profile_id UUID
        REFERENCES gts_student_medical_profile(id),

    alert_code VARCHAR(80) NOT NULL,
    alert_type VARCHAR(40) NOT NULL,
    alert_title VARCHAR(250) NOT NULL,
    alert_description VARCHAR(2000),

    severity VARCHAR(20) NOT NULL,
    emergency_instructions VARCHAR(2500),

    visible_to_teachers BOOLEAN NOT NULL DEFAULT FALSE,
    visible_to_transport_staff BOOLEAN NOT NULL DEFAULT FALSE,
    visible_to_catering_staff BOOLEAN NOT NULL DEFAULT FALSE,
    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    alert_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_medical_alert_code
        UNIQUE (tenant_id, student_id, alert_code),

    CONSTRAINT ck_gts_student_medical_alert_type
        CHECK (
            alert_type IN (
                'ALLERGY',
                'ANAPHYLAXIS',
                'ASTHMA',
                'EPILEPSY',
                'DIABETES',
                'CARDIAC',
                'MEDICATION',
                'DIETARY',
                'DISABILITY',
                'MENTAL_HEALTH',
                'INFECTIOUS_DISEASE',
                'EMERGENCY_PLAN',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_medical_alert_severity
        CHECK (
            severity IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_student_medical_alert_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_student_medical_alert_verification
        CHECK (
            verified = FALSE
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_medical_alert_lifecycle
        CHECK (
            alert_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'RESOLVED',
                'EXPIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_medical_alert_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_health_condition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    medical_profile_id UUID
        REFERENCES gts_student_medical_profile(id),

    condition_code VARCHAR(80),
    condition_name VARCHAR(250) NOT NULL,
    condition_category VARCHAR(40) NOT NULL,

    diagnosis_date DATE,
    diagnosed_by VARCHAR(250),

    condition_description VARCHAR(2000),
    management_plan VARCHAR(2500),

    chronic BOOLEAN NOT NULL DEFAULT FALSE,
    communicable BOOLEAN NOT NULL DEFAULT FALSE,
    emergency_risk BOOLEAN NOT NULL DEFAULT FALSE,

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    condition_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_student_health_condition_category
        CHECK (
            condition_category IN (
                'ALLERGY',
                'RESPIRATORY',
                'CARDIAC',
                'NEUROLOGICAL',
                'ENDOCRINE',
                'GASTROINTESTINAL',
                'MUSCULOSKELETAL',
                'DERMATOLOGICAL',
                'INFECTIOUS',
                'MENTAL_HEALTH',
                'DISABILITY',
                'VISION',
                'HEARING',
                'DENTAL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_health_condition_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED'
            )
        ),

    CONSTRAINT ck_gts_student_health_condition_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_health_condition_lifecycle
        CHECK (
            condition_status IN (
                'ACTIVE',
                'MANAGED',
                'RESOLVED',
                'IN_REMISSION',
                'MISDIAGNOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_health_condition_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_visit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    visit_reference VARCHAR(100) NOT NULL,

    health_facility_id UUID NOT NULL
        REFERENCES gts_health_facility(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    visit_type VARCHAR(40) NOT NULL,
    visit_reason VARCHAR(2500) NOT NULL,

    arrival_at TIMESTAMPTZ NOT NULL,
    departure_at TIMESTAMPTZ,

    arrived_from_location VARCHAR(250),
    accompanied_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    attending_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    triage_priority VARCHAR(20) NOT NULL DEFAULT 'ROUTINE',

    presenting_symptoms VARCHAR(3000),
    clinical_notes VARCHAR(4000),

    parent_contact_required BOOLEAN NOT NULL DEFAULT FALSE,
    emergency_response_required BOOLEAN NOT NULL DEFAULT FALSE,
    external_referral_required BOOLEAN NOT NULL DEFAULT FALSE,

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    workflow_instance_id UUID,

    visit_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_visit_reference
        UNIQUE (tenant_id, visit_reference),

    CONSTRAINT ck_gts_health_visit_type
        CHECK (
            visit_type IN (
                'ILLNESS',
                'INJURY',
                'FIRST_AID',
                'MEDICATION',
                'HEALTH_SCREENING',
                'FOLLOW_UP',
                'EMERGENCY',
                'MENTAL_WELLBEING',
                'IMMUNISATION',
                'RETURN_TO_SCHOOL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_visit_dates
        CHECK (
            departure_at IS NULL
            OR departure_at >= arrival_at
        ),

    CONSTRAINT ck_gts_health_visit_triage
        CHECK (
            triage_priority IN (
                'ROUTINE',
                'LOW',
                'MODERATE',
                'URGENT',
                'EMERGENCY'
            )
        ),

    CONSTRAINT ck_gts_health_visit_lifecycle
        CHECK (
            visit_status IN (
                'REGISTERED',
                'WAITING',
                'TRIAGED',
                'IN_TREATMENT',
                'OBSERVATION',
                'REFERRED',
                'DISCHARGED',
                'TRANSFERRED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_health_visit_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_vital_sign (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    health_visit_id UUID NOT NULL
        REFERENCES gts_health_visit(id) ON DELETE CASCADE,

    recorded_at TIMESTAMPTZ NOT NULL,
    recorded_by UUID,

    temperature_celsius NUMERIC(5,2),
    pulse_rate INTEGER,
    respiratory_rate INTEGER,

    systolic_blood_pressure INTEGER,
    diastolic_blood_pressure INTEGER,

    oxygen_saturation NUMERIC(5,2),

    height_centimetres NUMERIC(7,2),
    weight_kilograms NUMERIC(7,2),
    body_mass_index NUMERIC(6,2),

    pain_score INTEGER,
    consciousness_level VARCHAR(40),

    notes VARCHAR(1200),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_health_vital_temperature
        CHECK (
            temperature_celsius IS NULL
            OR (
                temperature_celsius >= 25
                AND temperature_celsius <= 50
            )
        ),

    CONSTRAINT ck_gts_health_vital_rates
        CHECK (
            (pulse_rate IS NULL OR pulse_rate > 0)
            AND (
                respiratory_rate IS NULL
                OR respiratory_rate > 0
            )
        ),

    CONSTRAINT ck_gts_health_vital_blood_pressure
        CHECK (
            (
                systolic_blood_pressure IS NULL
                OR systolic_blood_pressure > 0
            )
            AND (
                diastolic_blood_pressure IS NULL
                OR diastolic_blood_pressure > 0
            )
        ),

    CONSTRAINT ck_gts_health_vital_oxygen
        CHECK (
            oxygen_saturation IS NULL
            OR (
                oxygen_saturation >= 0
                AND oxygen_saturation <= 100
            )
        ),

    CONSTRAINT ck_gts_health_vital_measurements
        CHECK (
            (
                height_centimetres IS NULL
                OR height_centimetres > 0
            )
            AND (
                weight_kilograms IS NULL
                OR weight_kilograms > 0
            )
            AND (
                body_mass_index IS NULL
                OR body_mass_index > 0
            )
        ),

    CONSTRAINT ck_gts_health_vital_pain
        CHECK (
            pain_score IS NULL
            OR (
                pain_score >= 0
                AND pain_score <= 10
            )
        ),

    CONSTRAINT ck_gts_health_vital_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_assessment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    health_visit_id UUID NOT NULL
        REFERENCES gts_health_visit(id) ON DELETE CASCADE,

    assessment_type VARCHAR(40) NOT NULL,

    provisional_diagnosis VARCHAR(1000),
    assessment_findings VARCHAR(3000),
    treatment_recommendation VARCHAR(2500),

    severity VARCHAR(20),

    fit_to_return_to_class BOOLEAN,
    observation_required BOOLEAN NOT NULL DEFAULT FALSE,
    isolation_required BOOLEAN NOT NULL DEFAULT FALSE,
    external_referral_required BOOLEAN NOT NULL DEFAULT FALSE,

    assessed_at TIMESTAMPTZ NOT NULL,
    assessed_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_health_assessment_type
        CHECK (
            assessment_type IN (
                'TRIAGE',
                'CLINICAL',
                'FIRST_AID',
                'MENTAL_WELLBEING',
                'INJURY',
                'INFECTIOUS_DISEASE',
                'RETURN_TO_SCHOOL',
                'SCREENING',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_assessment_severity
        CHECK (
            severity IS NULL
            OR severity IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_health_assessment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_treatment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    treatment_reference VARCHAR(100) NOT NULL,

    health_visit_id UUID NOT NULL
        REFERENCES gts_health_visit(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    treatment_type VARCHAR(40) NOT NULL,
    treatment_description VARCHAR(2500) NOT NULL,

    first_aid_given BOOLEAN NOT NULL DEFAULT FALSE,
    consent_required BOOLEAN NOT NULL DEFAULT FALSE,
    consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    consent_recorded_at TIMESTAMPTZ,
    consent_recorded_by UUID,

    administered_at TIMESTAMPTZ NOT NULL,
    administered_by UUID,

    response_to_treatment VARCHAR(2000),
    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_date DATE,

    treatment_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_treatment_reference
        UNIQUE (tenant_id, treatment_reference),

    CONSTRAINT ck_gts_health_treatment_type
        CHECK (
            treatment_type IN (
                'FIRST_AID',
                'WOUND_CARE',
                'REST',
                'HYDRATION',
                'ICE_OR_HEAT',
                'MEDICATION',
                'NEBULISATION',
                'GLUCOSE_SUPPORT',
                'COUNSELLING_SUPPORT',
                'IMMOBILISATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_treatment_consent
        CHECK (
            consent_required = FALSE
            OR consent_recorded = TRUE
        ),

    CONSTRAINT ck_gts_health_treatment_consent_date
        CHECK (
            consent_recorded = FALSE
            OR consent_recorded_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_health_treatment_lifecycle
        CHECK (
            treatment_status IN (
                'PLANNED',
                'CONSENT_PENDING',
                'IN_PROGRESS',
                'COMPLETED',
                'STOPPED',
                'DECLINED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_health_treatment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_medication (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    medication_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    medical_profile_id UUID
        REFERENCES gts_student_medical_profile(id),

    medication_name VARCHAR(250) NOT NULL,
    generic_name VARCHAR(250),

    dosage VARCHAR(160) NOT NULL,
    route VARCHAR(40) NOT NULL,
    frequency VARCHAR(120) NOT NULL,

    indication VARCHAR(1000),

    prescribed_by VARCHAR(250),
    prescription_date DATE,

    start_date DATE NOT NULL,
    end_date DATE,

    administration_instructions VARCHAR(2000),
    storage_instructions VARCHAR(1000),

    guardian_consent_required BOOLEAN NOT NULL DEFAULT TRUE,
    guardian_consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_consent_at TIMESTAMPTZ,

    prescription_document_id UUID,

    medication_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_medication_reference
        UNIQUE (tenant_id, medication_reference),

    CONSTRAINT ck_gts_student_medication_route
        CHECK (
            route IN (
                'ORAL',
                'INHALATION',
                'TOPICAL',
                'OPHTHALMIC',
                'OTIC',
                'NASAL',
                'INJECTION',
                'RECTAL',
                'SUBLINGUAL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_medication_dates
        CHECK (
            end_date IS NULL
            OR end_date >= start_date
        ),

    CONSTRAINT ck_gts_student_medication_consent
        CHECK (
            guardian_consent_recorded = FALSE
            OR guardian_consent_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_medication_lifecycle
        CHECK (
            medication_status IN (
                'PENDING',
                'ACTIVE',
                'PAUSED',
                'COMPLETED',
                'DISCONTINUED',
                'EXPIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_medication_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_medication_administration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    administration_reference VARCHAR(100) NOT NULL,

    student_medication_id UUID NOT NULL
        REFERENCES gts_student_medication(id),

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    scheduled_at TIMESTAMPTZ,
    administered_at TIMESTAMPTZ,

    administered_dose VARCHAR(160),
    administered_by UUID,

    administration_result VARCHAR(30) NOT NULL DEFAULT 'ADMINISTERED',
    refusal_reason VARCHAR(1000),
    omission_reason VARCHAR(1000),

    adverse_reaction BOOLEAN NOT NULL DEFAULT FALSE,
    adverse_reaction_description VARCHAR(2000),

    witnessed_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_medication_administration_reference
        UNIQUE (tenant_id, administration_reference),

    CONSTRAINT ck_gts_medication_administration_result
        CHECK (
            administration_result IN (
                'ADMINISTERED',
                'REFUSED',
                'OMITTED',
                'UNAVAILABLE',
                'HELD',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_medication_administration_date
        CHECK (
            administration_result <> 'ADMINISTERED'
            OR administered_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_medication_administration_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_injury_incident (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    injury_reference VARCHAR(100) NOT NULL,

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    injury_date DATE NOT NULL,
    injury_time TIME,
    location_description VARCHAR(300),

    injury_type VARCHAR(40) NOT NULL,
    injury_description VARCHAR(3000) NOT NULL,
    severity VARCHAR(20) NOT NULL,

    activity_at_time VARCHAR(500),

    witnessed BOOLEAN NOT NULL DEFAULT FALSE,
    witness_details VARCHAR(1500),

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    guardian_notification_required BOOLEAN NOT NULL DEFAULT TRUE,
    authority_notification_required BOOLEAN NOT NULL DEFAULT FALSE,

    workflow_instance_id UUID,

    injury_status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_injury_reference
        UNIQUE (tenant_id, injury_reference),

    CONSTRAINT ck_gts_health_injury_type
        CHECK (
            injury_type IN (
                'CUT',
                'BRUISE',
                'BURN',
                'FRACTURE',
                'SPRAIN',
                'HEAD_INJURY',
                'EYE_INJURY',
                'DENTAL_INJURY',
                'ANIMAL_BITE',
                'INSECT_STING',
                'SPORTS_INJURY',
                'ROAD_TRAFFIC',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_injury_severity
        CHECK (
            severity IN (
                'MINOR',
                'MODERATE',
                'SERIOUS',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_health_injury_lifecycle
        CHECK (
            injury_status IN (
                'REPORTED',
                'ASSESSED',
                'TREATED',
                'REFERRED',
                'FOLLOW_UP',
                'RESOLVED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_health_injury_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_referral (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    referral_reference VARCHAR(100) NOT NULL,

    health_visit_id UUID NOT NULL
        REFERENCES gts_health_visit(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    referral_type VARCHAR(40) NOT NULL,
    referral_reason VARCHAR(2500) NOT NULL,

    urgency VARCHAR(20) NOT NULL,

    receiving_facility_name VARCHAR(250),
    receiving_provider_name VARCHAR(250),
    receiving_provider_contact VARCHAR(100),

    transport_method VARCHAR(40),
    accompanied_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    guardian_notified_at TIMESTAMPTZ,
    guardian_notification_request_id UUID,

    referred_at TIMESTAMPTZ NOT NULL,
    referred_by UUID,

    external_referral_reference VARCHAR(160),
    referral_document_id UUID,

    workflow_instance_id UUID,

    outcome_received_at TIMESTAMPTZ,
    outcome_summary VARCHAR(2500),

    referral_status VARCHAR(30) NOT NULL DEFAULT 'REFERRED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_referral_reference
        UNIQUE (tenant_id, referral_reference),

    CONSTRAINT ck_gts_health_referral_type
        CHECK (
            referral_type IN (
                'HOSPITAL',
                'CLINIC',
                'SPECIALIST',
                'DENTAL',
                'OPTICAL',
                'MENTAL_HEALTH',
                'EMERGENCY_SERVICE',
                'LABORATORY',
                'PHARMACY',
                'PUBLIC_HEALTH',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_referral_urgency
        CHECK (
            urgency IN (
                'ROUTINE',
                'PRIORITY',
                'URGENT',
                'EMERGENCY'
            )
        ),

    CONSTRAINT ck_gts_health_referral_transport
        CHECK (
            transport_method IS NULL
            OR transport_method IN (
                'SCHOOL_VEHICLE',
                'AMBULANCE',
                'GUARDIAN_VEHICLE',
                'PUBLIC_TRANSPORT',
                'WALKING',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_referral_lifecycle
        CHECK (
            referral_status IN (
                'PLANNED',
                'REFERRED',
                'ACCEPTED',
                'IN_TRANSIT',
                'RECEIVED',
                'COMPLETED',
                'DECLINED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_health_referral_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_guardian_communication (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    communication_reference VARCHAR(100) NOT NULL,

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    health_referral_id UUID
        REFERENCES gts_health_referral(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID NOT NULL
        REFERENCES gts_guardian(id),

    communication_type VARCHAR(40) NOT NULL,
    communication_channel VARCHAR(30) NOT NULL,

    communication_summary VARCHAR(2500),

    attempted_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    notification_request_id UUID,

    response_received BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_response VARCHAR(2000),

    communication_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_guardian_communication_reference
        UNIQUE (tenant_id, communication_reference),

    CONSTRAINT ck_gts_health_guardian_communication_type
        CHECK (
            communication_type IN (
                'ILLNESS_NOTICE',
                'INJURY_NOTICE',
                'MEDICATION_CONSENT',
                'EMERGENCY_NOTICE',
                'REFERRAL_NOTICE',
                'PICKUP_REQUEST',
                'FOLLOW_UP',
                'SCREENING_RESULT',
                'INFECTIOUS_DISEASE_NOTICE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_guardian_channel
        CHECK (
            communication_channel IN (
                'SMS',
                'EMAIL',
                'PHONE',
                'WHATSAPP',
                'PUSH',
                'IN_APP',
                'IN_PERSON',
                'LETTER'
            )
        ),

    CONSTRAINT ck_gts_health_guardian_communication_lifecycle
        CHECK (
            communication_status IN (
                'PENDING',
                'ATTEMPTED',
                'SENT',
                'DELIVERED',
                'ACKNOWLEDGED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_health_guardian_communication_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_immunisation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    immunisation_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    vaccine_code VARCHAR(80),
    vaccine_name VARCHAR(250) NOT NULL,

    dose_number INTEGER,
    administered_date DATE,
    next_due_date DATE,

    administered_by VARCHAR(250),
    facility_name VARCHAR(250),

    batch_number VARCHAR(120),
    certificate_document_id UUID,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    immunisation_status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_immunisation_reference
        UNIQUE (tenant_id, immunisation_reference),

    CONSTRAINT ck_gts_student_immunisation_dose
        CHECK (
            dose_number IS NULL
            OR dose_number > 0
        ),

    CONSTRAINT ck_gts_student_immunisation_dates
        CHECK (
            next_due_date IS NULL
            OR administered_date IS NULL
            OR next_due_date >= administered_date
        ),

    CONSTRAINT ck_gts_student_immunisation_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED'
            )
        ),

    CONSTRAINT ck_gts_student_immunisation_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_immunisation_lifecycle
        CHECK (
            immunisation_status IN (
                'RECORDED',
                'DUE',
                'OVERDUE',
                'COMPLETED',
                'EXEMPTED',
                'DECLINED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_immunisation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_screening (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    screening_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    academic_year_id UUID
        REFERENCES gts_academic_year(id),

    campus_id UUID
        REFERENCES gts_campus(id),

    screening_type VARCHAR(40) NOT NULL,
    screening_date DATE NOT NULL,

    screening_result VARCHAR(30) NOT NULL,
    findings VARCHAR(2500),

    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    referral_required BOOLEAN NOT NULL DEFAULT FALSE,

    conducted_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    external_provider_name VARCHAR(250),

    evidence_document_id UUID,

    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_notification_request_id UUID,
    guardian_notified_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_screening_reference
        UNIQUE (tenant_id, screening_reference),

    CONSTRAINT ck_gts_health_screening_type
        CHECK (
            screening_type IN (
                'GENERAL_HEALTH',
                'VISION',
                'HEARING',
                'DENTAL',
                'NUTRITION',
                'GROWTH',
                'MENTAL_WELLBEING',
                'INFECTIOUS_DISEASE',
                'DISABILITY',
                'SPORTS_FITNESS',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_screening_result
        CHECK (
            screening_result IN (
                'NORMAL',
                'OBSERVATION_REQUIRED',
                'FOLLOW_UP_REQUIRED',
                'REFERRAL_REQUIRED',
                'UNABLE_TO_COMPLETE'
            )
        ),

    CONSTRAINT ck_gts_health_screening_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_infectious_disease_case (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    case_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    disease_name VARCHAR(250) NOT NULL,
    disease_code VARCHAR(80),

    suspected BOOLEAN NOT NULL DEFAULT TRUE,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,

    identified_at TIMESTAMPTZ NOT NULL,
    confirmed_at TIMESTAMPTZ,

    isolation_required BOOLEAN NOT NULL DEFAULT FALSE,
    isolation_started_at TIMESTAMPTZ,
    isolation_ended_at TIMESTAMPTZ,

    public_health_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    public_health_notified_at TIMESTAMPTZ,
    public_health_reference VARCHAR(160),

    contact_tracing_required BOOLEAN NOT NULL DEFAULT FALSE,

    return_clearance_required BOOLEAN NOT NULL DEFAULT TRUE,

    case_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_infectious_disease_case_reference
        UNIQUE (tenant_id, case_reference),

    CONSTRAINT ck_gts_infectious_disease_confirmation
        CHECK (
            confirmed = FALSE
            OR confirmed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_infectious_disease_isolation
        CHECK (
            isolation_ended_at IS NULL
            OR isolation_started_at IS NULL
            OR isolation_ended_at >= isolation_started_at
        ),

    CONSTRAINT ck_gts_infectious_disease_lifecycle
        CHECK (
            case_status IN (
                'OPEN',
                'SUSPECTED',
                'CONFIRMED',
                'ISOLATED',
                'REFERRED',
                'RECOVERED',
                'CLEARED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_infectious_disease_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_return_to_school_clearance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    clearance_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    infectious_disease_case_id UUID
        REFERENCES gts_infectious_disease_case(id),

    absence_start_date DATE,
    absence_end_date DATE,

    clearance_date DATE NOT NULL,
    clearance_type VARCHAR(40) NOT NULL,

    fit_to_return BOOLEAN NOT NULL,
    restrictions VARCHAR(2000),
    follow_up_requirements VARCHAR(2000),

    medical_provider_name VARCHAR(250),
    medical_provider_contact VARCHAR(120),

    clearance_document_id UUID,

    verified_at TIMESTAMPTZ,
    verified_by UUID,

    clearance_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_return_to_school_clearance_reference
        UNIQUE (tenant_id, clearance_reference),

    CONSTRAINT ck_gts_return_to_school_absence_dates
        CHECK (
            absence_end_date IS NULL
            OR absence_start_date IS NULL
            OR absence_end_date >= absence_start_date
        ),

    CONSTRAINT ck_gts_return_to_school_clearance_type
        CHECK (
            clearance_type IN (
                'MEDICAL_CLEARANCE',
                'INFECTIOUS_DISEASE_CLEARANCE',
                'INJURY_CLEARANCE',
                'MENTAL_WELLBEING_CLEARANCE',
                'SPORTS_CLEARANCE',
                'POST_HOSPITALISATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_return_to_school_verification
        CHECK (
            clearance_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_return_to_school_lifecycle
        CHECK (
            clearance_status IN (
                'PENDING_VERIFICATION',
                'VERIFIED',
                'REJECTED',
                'RESTRICTIONS_APPLIED',
                'EXPIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_return_to_school_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    health_visit_id UUID
        REFERENCES gts_health_visit(id),

    document_type VARCHAR(50) NOT NULL,
    eds_document_id UUID NOT NULL,

    document_reference VARCHAR(160),
    document_date DATE,

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_health_document
        UNIQUE (
            tenant_id,
            student_id,
            document_type,
            eds_document_id
        ),

    CONSTRAINT ck_gts_health_document_type
        CHECK (
            document_type IN (
                'MEDICAL_REPORT',
                'PRESCRIPTION',
                'MEDICAL_CERTIFICATE',
                'IMMUNISATION_CERTIFICATE',
                'REFERRAL_LETTER',
                'LAB_RESULT',
                'DISCHARGE_SUMMARY',
                'FITNESS_CLEARANCE',
                'CONSENT_FORM',
                'EMERGENCY_PLAN',
                'SCREENING_REPORT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_health_document_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED'
            )
        ),

    CONSTRAINT ck_gts_health_document_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_health_document_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_health_history (
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

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_health_history_entity
        CHECK (
            entity_type IN (
                'HEALTH_FACILITY',
                'MEDICAL_ALERT',
                'HEALTH_CONDITION',
                'HEALTH_VISIT',
                'VITAL_SIGN',
                'HEALTH_ASSESSMENT',
                'TREATMENT',
                'MEDICATION',
                'MEDICATION_ADMINISTRATION',
                'INJURY',
                'REFERRAL',
                'GUARDIAN_COMMUNICATION',
                'IMMUNISATION',
                'SCREENING',
                'INFECTIOUS_DISEASE_CASE',
                'RETURN_CLEARANCE',
                'HEALTH_DOCUMENT'
            )
        ),

    CONSTRAINT ck_gts_health_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'ALERT_RAISED',
                'ALERT_RESOLVED',
                'VISIT_REGISTERED',
                'TRIAGED',
                'ASSESSED',
                'TREATMENT_STARTED',
                'TREATMENT_COMPLETED',
                'MEDICATION_REGISTERED',
                'MEDICATION_ADMINISTERED',
                'MEDICATION_REFUSED',
                'INJURY_REPORTED',
                'REFERRAL_CREATED',
                'GUARDIAN_NOTIFIED',
                'IMMUNISATION_RECORDED',
                'SCREENING_COMPLETED',
                'DISEASE_CASE_OPENED',
                'ISOLATION_STARTED',
                'ISOLATION_ENDED',
                'CLEARANCE_VERIFIED',
                'DISCHARGED',
                'CLOSED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_health_facility_campus
    ON gts_health_facility (
        tenant_id,
        campus_id,
        facility_type,
        facility_status
    );

CREATE INDEX ix_gts_student_medical_alert_student
    ON gts_student_medical_alert (
        tenant_id,
        student_id,
        severity,
        alert_status
    );

CREATE INDEX ix_gts_student_health_condition_student
    ON gts_student_health_condition (
        tenant_id,
        student_id,
        condition_category,
        condition_status
    );

CREATE INDEX ix_gts_health_visit_student
    ON gts_health_visit (
        tenant_id,
        student_id,
        arrival_at,
        visit_status
    );

CREATE INDEX ix_gts_health_visit_facility
    ON gts_health_visit (
        tenant_id,
        health_facility_id,
        arrival_at,
        triage_priority
    );

CREATE INDEX ix_gts_health_vital_visit
    ON gts_health_vital_sign (
        tenant_id,
        health_visit_id,
        recorded_at
    );

CREATE INDEX ix_gts_health_assessment_visit
    ON gts_health_assessment (
        tenant_id,
        health_visit_id,
        assessment_type
    );

CREATE INDEX ix_gts_health_treatment_visit
    ON gts_health_treatment (
        tenant_id,
        health_visit_id,
        treatment_status
    );

CREATE INDEX ix_gts_student_medication_student
    ON gts_student_medication (
        tenant_id,
        student_id,
        medication_status
    );

CREATE INDEX ix_gts_medication_administration_schedule
    ON gts_medication_administration (
        tenant_id,
        student_medication_id,
        scheduled_at,
        administration_result
    );

CREATE INDEX ix_gts_health_injury_student
    ON gts_health_injury_incident (
        tenant_id,
        student_id,
        injury_date,
        severity
    );

CREATE INDEX ix_gts_health_referral_student
    ON gts_health_referral (
        tenant_id,
        student_id,
        urgency,
        referral_status
    );

CREATE INDEX ix_gts_health_guardian_communication
    ON gts_health_guardian_communication (
        tenant_id,
        student_id,
        guardian_id,
        communication_status
    );

CREATE INDEX ix_gts_student_immunisation_due
    ON gts_student_immunisation (
        tenant_id,
        student_id,
        next_due_date,
        immunisation_status
    );

CREATE INDEX ix_gts_health_screening_student
    ON gts_health_screening (
        tenant_id,
        student_id,
        screening_type,
        screening_date
    );

CREATE INDEX ix_gts_infectious_disease_case_student
    ON gts_infectious_disease_case (
        tenant_id,
        student_id,
        case_status
    );

CREATE INDEX ix_gts_return_clearance_student
    ON gts_return_to_school_clearance (
        tenant_id,
        student_id,
        clearance_date,
        clearance_status
    );

CREATE INDEX ix_gts_health_document_student
    ON gts_health_document (
        tenant_id,
        student_id,
        document_type,
        verification_status
    );

CREATE INDEX ix_gts_health_history_entity
    ON gts_health_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '046',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
