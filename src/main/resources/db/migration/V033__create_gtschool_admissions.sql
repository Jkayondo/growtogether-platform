CREATE TABLE gts_admission_application (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    application_number VARCHAR(80) NOT NULL,
    academic_year_id UUID NOT NULL REFERENCES gts_academic_year(id),
    campus_id UUID NOT NULL REFERENCES gts_campus(id),
    desired_class_grade_id UUID NOT NULL REFERENCES gts_class_grade(id),
    desired_stream_id UUID REFERENCES gts_stream(id),

    application_date DATE NOT NULL DEFAULT CURRENT_DATE,
    admission_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    submission_channel VARCHAR(30) NOT NULL DEFAULT 'ONLINE',

    workflow_instance_id UUID,
    submitted_at TIMESTAMPTZ,
    submitted_by UUID,
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,
    decision_at TIMESTAMPTZ,
    decision_by UUID,
    decision_notes VARCHAR(2000),

    offered_class_grade_id UUID REFERENCES gts_class_grade(id),
    offered_stream_id UUID REFERENCES gts_stream(id),
    offer_expiry_date DATE,
    admitted_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_admission_application_number
        UNIQUE (tenant_id, application_number),

    CONSTRAINT ck_gts_admission_status
        CHECK (
            admission_status IN (
                'DRAFT',
                'SUBMITTED',
                'UNDER_REVIEW',
                'DOCUMENTS_REQUIRED',
                'INTERVIEW_REQUIRED',
                'ASSESSMENT_REQUIRED',
                'APPROVED',
                'WAITLISTED',
                'REJECTED',
                'OFFERED',
                'ACCEPTED',
                'DECLINED',
                'WITHDRAWN',
                'EXPIRED',
                'ENROLLED'
            )
        ),

    CONSTRAINT ck_gts_admission_submission_channel
        CHECK (
            submission_channel IN (
                'ONLINE',
                'OFFICE',
                'MOBILE',
                'IMPORT',
                'AGENT'
            )
        ),

    CONSTRAINT ck_gts_admission_application_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),

    CONSTRAINT ck_gts_admission_submission_time
        CHECK (
            admission_status = 'DRAFT'
            OR submitted_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_admission_offer_expiry
        CHECK (
            offer_expiry_date IS NULL
            OR offer_expiry_date >= application_date
        )
);

CREATE TABLE gts_admission_applicant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    first_name VARCHAR(120) NOT NULL,
    middle_name VARCHAR(120),
    last_name VARCHAR(120) NOT NULL,
    preferred_name VARCHAR(120),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(30),
    nationality_code VARCHAR(3),
    country_of_birth_code VARCHAR(3),
    primary_language VARCHAR(80),
    religion VARCHAR(100),

    national_id_number VARCHAR(120),
    passport_number VARCHAR(120),
    birth_certificate_number VARCHAR(120),

    email VARCHAR(200),
    phone_number VARCHAR(40),
    physical_address VARCHAR(500),

    existing_eiam_user_id UUID,
    existing_learner_reference UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_admission_applicant_application
        UNIQUE (tenant_id, admission_application_id),

    CONSTRAINT ck_gts_admission_applicant_birth_date
        CHECK (date_of_birth <= CURRENT_DATE),

    CONSTRAINT ck_gts_admission_applicant_gender
        CHECK (
            gender IS NULL
            OR gender IN (
                'FEMALE',
                'MALE',
                'OTHER',
                'NOT_DECLARED'
            )
        ),

    CONSTRAINT ck_gts_admission_applicant_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_admission_guardian (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    relationship_type VARCHAR(40) NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    middle_name VARCHAR(120),
    last_name VARCHAR(120) NOT NULL,

    phone_number VARCHAR(40) NOT NULL,
    alternative_phone_number VARCHAR(40),
    email VARCHAR(200),
    occupation VARCHAR(160),
    employer VARCHAR(200),
    physical_address VARCHAR(500),

    national_id_number VARCHAR(120),
    existing_eiam_user_id UUID,

    primary_guardian BOOLEAN NOT NULL DEFAULT FALSE,
    emergency_contact BOOLEAN NOT NULL DEFAULT FALSE,
    authorized_to_collect BOOLEAN NOT NULL DEFAULT TRUE,
    receives_communications BOOLEAN NOT NULL DEFAULT TRUE,
    financial_responsibility BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_admission_guardian_relationship
        CHECK (
            relationship_type IN (
                'FATHER',
                'MOTHER',
                'LEGAL_GUARDIAN',
                'GRANDPARENT',
                'SIBLING',
                'RELATIVE',
                'SPONSOR',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_admission_guardian_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_admission_primary_guardian
    ON gts_admission_guardian (tenant_id, admission_application_id)
    WHERE primary_guardian = TRUE AND status = 'ACTIVE';

CREATE TABLE gts_admission_previous_school (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    school_name VARCHAR(250) NOT NULL,
    school_code VARCHAR(100),
    country_code VARCHAR(3),
    district_or_region VARCHAR(160),
    last_class_or_grade VARCHAR(120),
    curriculum VARCHAR(160),
    start_date DATE,
    end_date DATE,
    leaving_reason VARCHAR(1000),
    academic_summary VARCHAR(2000),

    contact_name VARCHAR(200),
    contact_phone VARCHAR(40),
    contact_email VARCHAR(200),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_previous_school_dates
        CHECK (
            start_date IS NULL
            OR end_date IS NULL
            OR end_date >= start_date
        ),

    CONSTRAINT ck_gts_previous_school_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_admission_medical_declaration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    blood_group VARCHAR(10),
    allergies TEXT,
    chronic_conditions TEXT,
    current_medication TEXT,
    disabilities_or_special_needs TEXT,
    dietary_requirements TEXT,
    medical_notes TEXT,

    physician_name VARCHAR(200),
    physician_phone VARCHAR(40),

    emergency_contact_name VARCHAR(200),
    emergency_contact_relationship VARCHAR(100),
    emergency_contact_phone VARCHAR(40),

    consent_for_emergency_treatment BOOLEAN NOT NULL DEFAULT FALSE,
    declaration_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    declaration_confirmed_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_admission_medical_application
        UNIQUE (tenant_id, admission_application_id),

    CONSTRAINT ck_gts_admission_blood_group
        CHECK (
            blood_group IS NULL
            OR blood_group IN (
                'A+',
                'A-',
                'B+',
                'B-',
                'AB+',
                'AB-',
                'O+',
                'O-',
                'UNKNOWN'
            )
        ),

    CONSTRAINT ck_gts_admission_medical_confirmation
        CHECK (
            declaration_confirmed = FALSE
            OR declaration_confirmed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_admission_medical_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_admission_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    document_type VARCHAR(60) NOT NULL,
    eds_document_id UUID,
    document_reference VARCHAR(160),
    file_name VARCHAR(250),
    required_document BOOLEAN NOT NULL DEFAULT FALSE,
    received_at TIMESTAMPTZ,
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_admission_document_reference
        UNIQUE (
            tenant_id,
            admission_application_id,
            document_type,
            document_reference
        ),

    CONSTRAINT ck_gts_admission_document_type
        CHECK (
            document_type IN (
                'BIRTH_CERTIFICATE',
                'PASSPORT_PHOTO',
                'NATIONAL_ID',
                'PASSPORT',
                'PREVIOUS_REPORT_CARD',
                'TRANSFER_LETTER',
                'RECOMMENDATION_LETTER',
                'MEDICAL_FORM',
                'IMMUNIZATION_RECORD',
                'SPECIAL_NEEDS_ASSESSMENT',
                'SPONSORSHIP_DOCUMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_admission_document_verification
        CHECK (
            verification_status IN (
                'PENDING',
                'RECEIVED',
                'VERIFIED',
                'REJECTED',
                'WAIVED'
            )
        ),

    CONSTRAINT ck_gts_admission_document_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_admission_decision (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    decision_type VARCHAR(30) NOT NULL,
    decision_reason VARCHAR(2000),
    decided_at TIMESTAMPTZ NOT NULL,
    decided_by UUID NOT NULL,

    offered_class_grade_id UUID REFERENCES gts_class_grade(id),
    offered_stream_id UUID REFERENCES gts_stream(id),
    offer_expiry_date DATE,

    workflow_instance_id UUID,
    workflow_task_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_admission_decision_type
        CHECK (
            decision_type IN (
                'APPROVE',
                'REJECT',
                'WAITLIST',
                'REQUEST_DOCUMENTS',
                'REQUEST_INTERVIEW',
                'REQUEST_ASSESSMENT',
                'WITHDRAW_OFFER'
            )
        ),

    CONSTRAINT ck_gts_admission_decision_offer
        CHECK (
            decision_type <> 'APPROVE'
            OR offered_class_grade_id IS NOT NULL
        ),

    CONSTRAINT ck_gts_admission_decision_status
        CHECK (status IN ('ACTIVE', 'SUPERSEDED', 'ARCHIVED'))
);

CREATE TABLE gts_admission_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    change_reason VARCHAR(1000),
    changed_at TIMESTAMPTZ NOT NULL,
    changed_by UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_admission_history_new_status
        CHECK (
            new_status IN (
                'DRAFT',
                'SUBMITTED',
                'UNDER_REVIEW',
                'DOCUMENTS_REQUIRED',
                'INTERVIEW_REQUIRED',
                'ASSESSMENT_REQUIRED',
                'APPROVED',
                'WAITLISTED',
                'REJECTED',
                'OFFERED',
                'ACCEPTED',
                'DECLINED',
                'WITHDRAWN',
                'EXPIRED',
                'ENROLLED'
            )
        )
);

CREATE INDEX ix_gts_admission_application_status
    ON gts_admission_application (
        tenant_id,
        admission_status,
        application_date
    );

CREATE INDEX ix_gts_admission_application_academic_year
    ON gts_admission_application (
        tenant_id,
        academic_year_id,
        campus_id
    );

CREATE INDEX ix_gts_admission_application_desired_class
    ON gts_admission_application (
        tenant_id,
        desired_class_grade_id,
        admission_status
    );

CREATE INDEX ix_gts_admission_applicant_name
    ON gts_admission_applicant (
        tenant_id,
        last_name,
        first_name
    );

CREATE INDEX ix_gts_admission_applicant_birth_date
    ON gts_admission_applicant (
        tenant_id,
        date_of_birth
    );

CREATE INDEX ix_gts_admission_guardian_application
    ON gts_admission_guardian (
        tenant_id,
        admission_application_id,
        relationship_type
    );

CREATE INDEX ix_gts_admission_previous_school_application
    ON gts_admission_previous_school (
        tenant_id,
        admission_application_id
    );

CREATE INDEX ix_gts_admission_document_application
    ON gts_admission_document (
        tenant_id,
        admission_application_id,
        verification_status
    );

CREATE INDEX ix_gts_admission_document_eds
    ON gts_admission_document (
        tenant_id,
        eds_document_id
    )
    WHERE eds_document_id IS NOT NULL;

CREATE INDEX ix_gts_admission_decision_application
    ON gts_admission_decision (
        tenant_id,
        admission_application_id,
        decided_at DESC
    );

CREATE INDEX ix_gts_admission_history_application
    ON gts_admission_status_history (
        tenant_id,
        admission_application_id,
        changed_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '033',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
