CREATE TABLE gts_guardian (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    guardian_number VARCHAR(80) NOT NULL,

    first_name VARCHAR(120) NOT NULL,
    middle_name VARCHAR(120),
    last_name VARCHAR(120) NOT NULL,
    preferred_name VARCHAR(120),

    date_of_birth DATE,
    gender VARCHAR(30),
    nationality_code VARCHAR(3),

    national_id_number VARCHAR(120),
    passport_number VARCHAR(120),

    primary_phone_number VARCHAR(40) NOT NULL,
    alternative_phone_number VARCHAR(40),
    email VARCHAR(200),
    physical_address VARCHAR(500),
    postal_address VARCHAR(500),

    occupation VARCHAR(160),
    employer VARCHAR(200),

    eiam_user_id UUID,
    source_admission_guardian_id UUID
        REFERENCES gts_admission_guardian(id),

    preferred_language VARCHAR(80),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    guardian_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_guardian_number
        UNIQUE (tenant_id, guardian_number),

    CONSTRAINT uq_gts_guardian_source_admission
        UNIQUE (tenant_id, source_admission_guardian_id),

    CONSTRAINT ck_gts_guardian_birth_date
        CHECK (
            date_of_birth IS NULL
            OR date_of_birth <= CURRENT_DATE
        ),

    CONSTRAINT ck_gts_guardian_gender
        CHECK (
            gender IS NULL
            OR gender IN (
                'FEMALE',
                'MALE',
                'OTHER',
                'NOT_DECLARED'
            )
        ),

    CONSTRAINT ck_gts_guardian_verification_status
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'EXPIRED'
            )
        ),

    CONSTRAINT ck_gts_guardian_verification
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_guardian_lifecycle
        CHECK (
            guardian_status IN (
                'ACTIVE',
                'INACTIVE',
                'DECEASED',
                'RESTRICTED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_guardian_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_guardian_relationship (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    guardian_id UUID NOT NULL
        REFERENCES gts_guardian(id),

    relationship_type VARCHAR(40) NOT NULL,
    relationship_description VARCHAR(300),

    legal_guardian BOOLEAN NOT NULL DEFAULT FALSE,
    primary_guardian BOOLEAN NOT NULL DEFAULT FALSE,
    emergency_contact BOOLEAN NOT NULL DEFAULT FALSE,

    has_custody BOOLEAN NOT NULL DEFAULT FALSE,
    custody_type VARCHAR(30),
    custody_notes VARCHAR(1000),

    lives_with_student BOOLEAN NOT NULL DEFAULT FALSE,
    authorized_to_collect BOOLEAN NOT NULL DEFAULT FALSE,
    receives_communications BOOLEAN NOT NULL DEFAULT TRUE,
    receives_academic_information BOOLEAN NOT NULL DEFAULT TRUE,
    receives_discipline_information BOOLEAN NOT NULL DEFAULT TRUE,
    receives_medical_information BOOLEAN NOT NULL DEFAULT FALSE,
    may_approve_school_activities BOOLEAN NOT NULL DEFAULT FALSE,

    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    relationship_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_guardian_relationship
        UNIQUE (tenant_id, student_id, guardian_id),

    CONSTRAINT ck_gts_student_guardian_relationship_type
        CHECK (
            relationship_type IN (
                'FATHER',
                'MOTHER',
                'LEGAL_GUARDIAN',
                'STEP_PARENT',
                'GRANDPARENT',
                'SIBLING',
                'RELATIVE',
                'FOSTER_PARENT',
                'SPONSOR',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_guardian_custody_type
        CHECK (
            custody_type IS NULL
            OR custody_type IN (
                'SOLE',
                'JOINT',
                'TEMPORARY',
                'COURT_APPOINTED',
                'CUSTOMARY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_guardian_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_student_guardian_lifecycle
        CHECK (
            relationship_status IN (
                'ACTIVE',
                'SUSPENDED',
                'RESTRICTED',
                'ENDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_guardian_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_student_primary_guardian
    ON gts_student_guardian_relationship (
        tenant_id,
        student_id
    )
    WHERE primary_guardian = TRUE
      AND relationship_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE gts_guardian_communication_preference (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    guardian_id UUID NOT NULL
        REFERENCES gts_guardian(id) ON DELETE CASCADE,

    communication_channel VARCHAR(30) NOT NULL,
    communication_category VARCHAR(40) NOT NULL,

    destination VARCHAR(250),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority_number INTEGER NOT NULL DEFAULT 1,

    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(80),

    consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    consent_recorded_at TIMESTAMPTZ,
    consent_source VARCHAR(80),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_guardian_communication_preference
        UNIQUE (
            tenant_id,
            guardian_id,
            communication_channel,
            communication_category
        ),

    CONSTRAINT ck_gts_guardian_communication_channel
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

    CONSTRAINT ck_gts_guardian_communication_category
        CHECK (
            communication_category IN (
                'GENERAL',
                'ACADEMIC',
                'ATTENDANCE',
                'DISCIPLINE',
                'FINANCE',
                'MEDICAL',
                'EMERGENCY',
                'TRANSPORT',
                'EVENTS',
                'MARKETING'
            )
        ),

    CONSTRAINT ck_gts_guardian_communication_priority
        CHECK (priority_number > 0),

    CONSTRAINT ck_gts_guardian_communication_consent
        CHECK (
            consent_recorded = FALSE
            OR consent_recorded_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_guardian_communication_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_pickup_authorization (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    guardian_id UUID
        REFERENCES gts_guardian(id),

    authorized_person_name VARCHAR(200) NOT NULL,
    relationship_to_student VARCHAR(100),
    phone_number VARCHAR(40) NOT NULL,

    national_id_number VARCHAR(120),
    identification_notes VARCHAR(500),
    eds_photo_document_id UUID,

    authorization_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR',
    effective_from DATE NOT NULL,
    effective_to DATE,

    approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    revoked_at TIMESTAMPTZ,
    revoked_by UUID,
    revocation_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_pickup_authorization_type
        CHECK (
            authorization_type IN (
                'REGULAR',
                'TEMPORARY',
                'EMERGENCY',
                'TRANSPORT_DELEGATE'
            )
        ),

    CONSTRAINT ck_gts_pickup_authorization_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_pickup_approval_status
        CHECK (
            approval_status IN (
                'PENDING',
                'APPROVED',
                'REJECTED',
                'REVOKED',
                'EXPIRED'
            )
        ),

    CONSTRAINT ck_gts_pickup_approval
        CHECK (
            approval_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_pickup_revocation
        CHECK (
            approval_status <> 'REVOKED'
            OR revoked_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_pickup_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_financial_responsibility (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    guardian_id UUID
        REFERENCES gts_guardian(id),

    responsible_party_type VARCHAR(30) NOT NULL DEFAULT 'GUARDIAN',
    responsible_party_name VARCHAR(200),
    organization_reference UUID,

    responsibility_type VARCHAR(30) NOT NULL,
    responsibility_percentage NUMERIC(5,2),

    billing_contact BOOLEAN NOT NULL DEFAULT FALSE,
    receives_invoices BOOLEAN NOT NULL DEFAULT TRUE,
    receives_payment_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    may_enter_payment_arrangements BOOLEAN NOT NULL DEFAULT FALSE,

    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    approval_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    workflow_instance_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_financial_party_type
        CHECK (
            responsible_party_type IN (
                'GUARDIAN',
                'SPONSOR',
                'ORGANIZATION',
                'GOVERNMENT',
                'SCHOLARSHIP',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_financial_responsibility_type
        CHECK (
            responsibility_type IN (
                'FULL',
                'PARTIAL',
                'TUITION',
                'BOARDING',
                'TRANSPORT',
                'MEDICAL',
                'ACTIVITIES',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_financial_responsibility_percentage
        CHECK (
            responsibility_percentage IS NULL
            OR (
                responsibility_percentage > 0
                AND responsibility_percentage <= 100
            )
        ),

    CONSTRAINT ck_gts_financial_responsibility_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_financial_approval_status
        CHECK (
            approval_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'ENDED',
                'REJECTED'
            )
        ),

    CONSTRAINT ck_gts_financial_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_guardian_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    guardian_id UUID NOT NULL
        REFERENCES gts_guardian(id) ON DELETE CASCADE,

    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    change_reason VARCHAR(1000),

    effective_at TIMESTAMPTZ NOT NULL,
    changed_by UUID,
    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_guardian_history_status
        CHECK (
            new_status IN (
                'ACTIVE',
                'INACTIVE',
                'DECEASED',
                'RESTRICTED',
                'ARCHIVED'
            )
        )
);

CREATE TABLE gts_student_guardian_relationship_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    relationship_id UUID NOT NULL
        REFERENCES gts_student_guardian_relationship(id)
        ON DELETE CASCADE,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID NOT NULL
        REFERENCES gts_guardian(id),

    event_type VARCHAR(40) NOT NULL,
    event_reason VARCHAR(1000),

    event_at TIMESTAMPTZ NOT NULL,
    event_by UUID,
    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_guardian_relationship_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'CUSTODY_CHANGED',
                'COMMUNICATION_CHANGED',
                'PICKUP_AUTHORIZATION_CHANGED',
                'FINANCIAL_RESPONSIBILITY_CHANGED',
                'SUSPENDED',
                'RESTRICTED',
                'ENDED',
                'REACTIVATED'
            )
        )
);

CREATE INDEX ix_gts_guardian_name
    ON gts_guardian (
        tenant_id,
        last_name,
        first_name
    );

CREATE INDEX ix_gts_guardian_phone
    ON gts_guardian (
        tenant_id,
        primary_phone_number
    );

CREATE INDEX ix_gts_guardian_eiam_user
    ON gts_guardian (
        tenant_id,
        eiam_user_id
    )
    WHERE eiam_user_id IS NOT NULL;

CREATE INDEX ix_gts_student_guardian_student
    ON gts_student_guardian_relationship (
        tenant_id,
        student_id,
        relationship_status
    );

CREATE INDEX ix_gts_student_guardian_guardian
    ON gts_student_guardian_relationship (
        tenant_id,
        guardian_id,
        relationship_status
    );

CREATE INDEX ix_gts_guardian_communication
    ON gts_guardian_communication_preference (
        tenant_id,
        guardian_id,
        enabled
    );

CREATE INDEX ix_gts_pickup_student
    ON gts_student_pickup_authorization (
        tenant_id,
        student_id,
        approval_status
    );

CREATE INDEX ix_gts_pickup_guardian
    ON gts_student_pickup_authorization (
        tenant_id,
        guardian_id
    )
    WHERE guardian_id IS NOT NULL;

CREATE INDEX ix_gts_financial_responsibility_student
    ON gts_student_financial_responsibility (
        tenant_id,
        student_id,
        approval_status
    );

CREATE INDEX ix_gts_financial_responsibility_guardian
    ON gts_student_financial_responsibility (
        tenant_id,
        guardian_id
    )
    WHERE guardian_id IS NOT NULL;

CREATE INDEX ix_gts_guardian_status_history
    ON gts_guardian_status_history (
        tenant_id,
        guardian_id,
        effective_at DESC
    );

CREATE INDEX ix_gts_guardian_relationship_history
    ON gts_student_guardian_relationship_history (
        tenant_id,
        relationship_id,
        event_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '035',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
