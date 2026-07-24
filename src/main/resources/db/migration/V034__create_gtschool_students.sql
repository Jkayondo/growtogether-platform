CREATE TABLE gts_student (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    admission_application_id UUID
        REFERENCES gts_admission_application(id),

    student_number VARCHAR(80) NOT NULL,
    permanent_learner_number VARCHAR(120) NOT NULL,

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

    email VARCHAR(200),
    phone_number VARCHAR(40),
    physical_address VARCHAR(500),

    eiam_user_id UUID,
    eds_student_file_id UUID,

    admission_date DATE,
    first_enrollment_date DATE,
    expected_completion_date DATE,
    completion_date DATE,

    student_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_number
        UNIQUE (tenant_id, student_number),

    CONSTRAINT uq_gts_student_permanent_learner_number
        UNIQUE (permanent_learner_number),

    CONSTRAINT uq_gts_student_admission_application
        UNIQUE (tenant_id, admission_application_id),

    CONSTRAINT ck_gts_student_birth_date
        CHECK (date_of_birth <= CURRENT_DATE),

    CONSTRAINT ck_gts_student_gender
        CHECK (
            gender IS NULL
            OR gender IN (
                'FEMALE',
                'MALE',
                'OTHER',
                'NOT_DECLARED'
            )
        ),

    CONSTRAINT ck_gts_student_lifecycle
        CHECK (
            student_status IN (
                'PENDING_ENROLLMENT',
                'ACTIVE',
                'SUSPENDED',
                'ON_LEAVE',
                'WITHDRAWN',
                'TRANSFERRED',
                'GRADUATED',
                'DECEASED',
                'ALUMNI',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_dates
        CHECK (
            completion_date IS NULL
            OR first_enrollment_date IS NULL
            OR completion_date >= first_enrollment_date
        ),

    CONSTRAINT ck_gts_student_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_enrollment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

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

    enrollment_number VARCHAR(100) NOT NULL,
    enrollment_date DATE NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,

    enrollment_type VARCHAR(30) NOT NULL DEFAULT 'NEW',
    enrollment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    previous_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    workflow_instance_id UUID,

    enrolled_by UUID,
    approved_by UUID,
    approved_at TIMESTAMPTZ,

    exit_date DATE,
    exit_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_enrollment_number
        UNIQUE (tenant_id, enrollment_number),

    CONSTRAINT uq_gts_student_enrollment_period
        UNIQUE (
            tenant_id,
            student_id,
            academic_year_id,
            academic_term_id
        ),

    CONSTRAINT ck_gts_student_enrollment_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_student_enrollment_exit_date
        CHECK (
            exit_date IS NULL
            OR exit_date >= enrollment_date
        ),

    CONSTRAINT ck_gts_student_enrollment_type
        CHECK (
            enrollment_type IN (
                'NEW',
                'CONTINUING',
                'REPEAT',
                'TRANSFER_IN',
                'REINSTATEMENT',
                'PROMOTION'
            )
        ),

    CONSTRAINT ck_gts_student_enrollment_status
        CHECK (
            enrollment_status IN (
                'PENDING',
                'ACTIVE',
                'COMPLETED',
                'SUSPENDED',
                'WITHDRAWN',
                'TRANSFERRED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_student_enrollment_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_student_active_enrollment
    ON gts_student_enrollment (tenant_id, student_id)
    WHERE enrollment_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE gts_student_identifier (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    identifier_type VARCHAR(40) NOT NULL,
    identifier_value VARCHAR(160) NOT NULL,
    issuing_authority VARCHAR(200),
    country_code VARCHAR(3),

    issued_at DATE,
    expires_at DATE,

    primary_identifier BOOLEAN NOT NULL DEFAULT FALSE,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_identifier
        UNIQUE (
            tenant_id,
            identifier_type,
            identifier_value
        ),

    CONSTRAINT ck_gts_student_identifier_type
        CHECK (
            identifier_type IN (
                'STUDENT_NUMBER',
                'PERMANENT_LEARNER_NUMBER',
                'NATIONAL_ID',
                'PASSPORT',
                'BIRTH_CERTIFICATE',
                'MINISTRY_REGISTRATION',
                'EXAMINATION_NUMBER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_identifier_dates
        CHECK (
            expires_at IS NULL
            OR issued_at IS NULL
            OR expires_at >= issued_at
        ),

    CONSTRAINT ck_gts_student_identifier_verification
        CHECK (
            verified = FALSE
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_identifier_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_student_primary_identifier
    ON gts_student_identifier (
        tenant_id,
        student_id,
        identifier_type
    )
    WHERE primary_identifier = TRUE
      AND status = 'ACTIVE';

CREATE TABLE gts_student_emergency_contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    contact_name VARCHAR(200) NOT NULL,
    relationship_type VARCHAR(60) NOT NULL,
    phone_number VARCHAR(40) NOT NULL,
    alternative_phone_number VARCHAR(40),
    email VARCHAR(200),
    physical_address VARCHAR(500),

    priority_number INTEGER NOT NULL DEFAULT 1,
    authorized_to_collect BOOLEAN NOT NULL DEFAULT FALSE,
    receives_emergency_notifications BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_emergency_priority
        UNIQUE (tenant_id, student_id, priority_number),

    CONSTRAINT ck_gts_student_emergency_priority
        CHECK (priority_number > 0),

    CONSTRAINT ck_gts_student_emergency_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_medical_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    source_admission_medical_id UUID
        REFERENCES gts_admission_medical_declaration(id),

    blood_group VARCHAR(10),
    allergies TEXT,
    chronic_conditions TEXT,
    current_medication TEXT,
    disabilities_or_special_needs TEXT,
    dietary_requirements TEXT,
    medical_notes TEXT,

    physician_name VARCHAR(200),
    physician_phone VARCHAR(40),

    emergency_treatment_consent BOOLEAN NOT NULL DEFAULT FALSE,
    consent_recorded_at TIMESTAMPTZ,
    consent_recorded_by UUID,

    confidential_record BOOLEAN NOT NULL DEFAULT TRUE,
    last_reviewed_at TIMESTAMPTZ,
    last_reviewed_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_medical_profile
        UNIQUE (tenant_id, student_id),

    CONSTRAINT ck_gts_student_medical_blood_group
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

    CONSTRAINT ck_gts_student_medical_consent
        CHECK (
            emergency_treatment_consent = FALSE
            OR consent_recorded_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_medical_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id) ON DELETE CASCADE,

    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    change_reason VARCHAR(1000),

    effective_at TIMESTAMPTZ NOT NULL,
    changed_by UUID,
    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_student_history_status
        CHECK (
            new_status IN (
                'PENDING_ENROLLMENT',
                'ACTIVE',
                'SUSPENDED',
                'ON_LEAVE',
                'WITHDRAWN',
                'TRANSFERRED',
                'GRADUATED',
                'DECEASED',
                'ALUMNI',
                'ARCHIVED'
            )
        )
);

CREATE INDEX ix_gts_student_name
    ON gts_student (
        tenant_id,
        last_name,
        first_name
    );

CREATE INDEX ix_gts_student_status
    ON gts_student (
        tenant_id,
        student_status
    );

CREATE INDEX ix_gts_student_eiam_user
    ON gts_student (
        tenant_id,
        eiam_user_id
    )
    WHERE eiam_user_id IS NOT NULL;

CREATE INDEX ix_gts_student_admission
    ON gts_student (
        tenant_id,
        admission_application_id
    )
    WHERE admission_application_id IS NOT NULL;

CREATE INDEX ix_gts_student_enrollment_student
    ON gts_student_enrollment (
        tenant_id,
        student_id,
        academic_year_id
    );

CREATE INDEX ix_gts_student_enrollment_class
    ON gts_student_enrollment (
        tenant_id,
        class_grade_id,
        stream_id,
        enrollment_status
    );

CREATE INDEX ix_gts_student_identifier_student
    ON gts_student_identifier (
        tenant_id,
        student_id,
        identifier_type
    );

CREATE INDEX ix_gts_student_emergency_contact
    ON gts_student_emergency_contact (
        tenant_id,
        student_id,
        priority_number
    );

CREATE INDEX ix_gts_student_medical_review
    ON gts_student_medical_profile (
        tenant_id,
        last_reviewed_at
    );

CREATE INDEX ix_gts_student_status_history
    ON gts_student_status_history (
        tenant_id,
        student_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '034',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
