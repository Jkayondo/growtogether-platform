CREATE TABLE ewf_workforce_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_number VARCHAR(80) NOT NULL,
    employee_number VARCHAR(80),

    first_name VARCHAR(120) NOT NULL,
    middle_name VARCHAR(120),
    last_name VARCHAR(120) NOT NULL,
    preferred_name VARCHAR(120),

    date_of_birth DATE,
    gender VARCHAR(30),
    nationality_code VARCHAR(3),

    national_id_number VARCHAR(120),
    passport_number VARCHAR(120),

    primary_phone_number VARCHAR(40),
    alternative_phone_number VARCHAR(40),
    email VARCHAR(200),
    physical_address VARCHAR(500),
    postal_address VARCHAR(500),

    eiam_user_id UUID,
    eds_personnel_file_id UUID,

    workforce_category VARCHAR(40) NOT NULL DEFAULT 'EMPLOYEE',
    workforce_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_workforce_number
        UNIQUE (tenant_id, workforce_number),

    CONSTRAINT uq_ewf_employee_number
        UNIQUE (tenant_id, employee_number),

    CONSTRAINT ck_ewf_workforce_gender
        CHECK (
            gender IS NULL
            OR gender IN (
                'FEMALE',
                'MALE',
                'OTHER',
                'NOT_DECLARED'
            )
        ),

    CONSTRAINT ck_ewf_workforce_category
        CHECK (
            workforce_category IN (
                'EMPLOYEE',
                'CONTRACTOR',
                'CONSULTANT',
                'VOLUNTEER',
                'INTERN',
                'APPRENTICE',
                'TEMPORARY',
                'BOARD_MEMBER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_workforce_lifecycle
        CHECK (
            workforce_status IN (
                'PROSPECTIVE',
                'ONBOARDING',
                'ACTIVE',
                'ON_LEAVE',
                'SUSPENDED',
                'INACTIVE',
                'RESIGNED',
                'RETIRED',
                'TERMINATED',
                'DECEASED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_ewf_workforce_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_organizational_unit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    parent_unit_id UUID
        REFERENCES ewf_organizational_unit(id),

    unit_code VARCHAR(80) NOT NULL,
    unit_name VARCHAR(200) NOT NULL,
    unit_type VARCHAR(40) NOT NULL,

    description VARCHAR(1000),
    location_reference UUID,
    manager_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    effective_from DATE,
    effective_to DATE,

    unit_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_organizational_unit_code
        UNIQUE (tenant_id, unit_code),

    CONSTRAINT ck_ewf_organizational_unit_type
        CHECK (
            unit_type IN (
                'ORGANIZATION',
                'DIRECTORATE',
                'DIVISION',
                'DEPARTMENT',
                'BRANCH',
                'CAMPUS',
                'FACILITY',
                'SECTION',
                'TEAM',
                'PROJECT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_organizational_unit_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_ewf_organizational_unit_lifecycle
        CHECK (
            unit_status IN (
                'PLANNED',
                'ACTIVE',
                'SUSPENDED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_ewf_organizational_unit_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_position (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    organizational_unit_id UUID
        REFERENCES ewf_organizational_unit(id),

    reports_to_position_id UUID
        REFERENCES ewf_position(id),

    position_code VARCHAR(80) NOT NULL,
    position_title VARCHAR(200) NOT NULL,
    position_description VARCHAR(1500),

    employment_category VARCHAR(40),
    grade_code VARCHAR(80),
    level_code VARCHAR(80),

    approved_capacity INTEGER NOT NULL DEFAULT 1,
    filled_capacity INTEGER NOT NULL DEFAULT 0,

    position_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_position_code
        UNIQUE (tenant_id, position_code),

    CONSTRAINT ck_ewf_position_capacity
        CHECK (
            approved_capacity > 0
            AND filled_capacity >= 0
            AND filled_capacity <= approved_capacity
        ),

    CONSTRAINT ck_ewf_position_lifecycle
        CHECK (
            position_status IN (
                'PLANNED',
                'ACTIVE',
                'FROZEN',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_ewf_position_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_employment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id),

    employment_reference VARCHAR(100) NOT NULL,
    employment_type VARCHAR(40) NOT NULL,
    contract_type VARCHAR(40),

    employment_start_date DATE NOT NULL,
    employment_end_date DATE,

    probation_start_date DATE,
    probation_end_date DATE,
    confirmation_date DATE,

    full_time_equivalent NUMERIC(5,2) NOT NULL DEFAULT 100.00,

    payroll_reference VARCHAR(120),
    tax_reference VARCHAR(120),
    pension_reference VARCHAR(120),
    salary_grade_code VARCHAR(80),

    employment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    workflow_instance_id UUID,
    exit_date DATE,
    exit_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_employment_reference
        UNIQUE (tenant_id, employment_reference),

    CONSTRAINT ck_ewf_employment_type
        CHECK (
            employment_type IN (
                'PERMANENT',
                'FIXED_TERM',
                'TEMPORARY',
                'PART_TIME',
                'CASUAL',
                'CONSULTANCY',
                'VOLUNTEER',
                'INTERNSHIP',
                'APPRENTICESHIP',
                'SECONDMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_contract_type
        CHECK (
            contract_type IS NULL
            OR contract_type IN (
                'OPEN_ENDED',
                'FIXED_DURATION',
                'TASK_BASED',
                'PROBATIONARY',
                'SERVICE_AGREEMENT',
                'VOLUNTEER_AGREEMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_employment_dates
        CHECK (
            employment_end_date IS NULL
            OR employment_end_date >= employment_start_date
        ),

    CONSTRAINT ck_ewf_probation_dates
        CHECK (
            probation_end_date IS NULL
            OR probation_start_date IS NULL
            OR probation_end_date >= probation_start_date
        ),

    CONSTRAINT ck_ewf_employment_fte
        CHECK (
            full_time_equivalent > 0
            AND full_time_equivalent <= 100
        ),

    CONSTRAINT ck_ewf_employment_lifecycle
        CHECK (
            employment_status IN (
                'PENDING',
                'PROBATION',
                'ACTIVE',
                'ON_LEAVE',
                'SUSPENDED',
                'ENDED',
                'RESIGNED',
                'RETIRED',
                'TERMINATED',
                'EXPIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_ewf_employment_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id),

    employment_id UUID
        REFERENCES ewf_employment(id),

    position_id UUID NOT NULL
        REFERENCES ewf_position(id),

    organizational_unit_id UUID
        REFERENCES ewf_organizational_unit(id),

    reports_to_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    assignment_reference VARCHAR(100) NOT NULL,
    assignment_type VARCHAR(40) NOT NULL DEFAULT 'PRIMARY',

    effective_from DATE NOT NULL,
    effective_to DATE,

    workload_percentage NUMERIC(5,2) NOT NULL DEFAULT 100.00,

    primary_assignment BOOLEAN NOT NULL DEFAULT FALSE,
    acting_assignment BOOLEAN NOT NULL DEFAULT FALSE,

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

    CONSTRAINT uq_ewf_assignment_reference
        UNIQUE (tenant_id, assignment_reference),

    CONSTRAINT ck_ewf_assignment_type
        CHECK (
            assignment_type IN (
                'PRIMARY',
                'SECONDARY',
                'ACTING',
                'TEMPORARY',
                'PROJECT',
                'SECONDMENT',
                'RELIEF',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_assignment_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_ewf_assignment_workload
        CHECK (
            workload_percentage > 0
            AND workload_percentage <= 100
        ),

    CONSTRAINT ck_ewf_assignment_lifecycle
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

    CONSTRAINT ck_ewf_assignment_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_ewf_primary_assignment
    ON ewf_assignment (tenant_id, workforce_member_id)
    WHERE primary_assignment = TRUE
      AND assignment_status = 'ACTIVE'
      AND status = 'ACTIVE';

CREATE TABLE ewf_qualification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id) ON DELETE CASCADE,

    qualification_type VARCHAR(40) NOT NULL,
    qualification_title VARCHAR(250) NOT NULL,
    field_of_study VARCHAR(200),

    awarding_institution VARCHAR(250) NOT NULL,
    country_code VARCHAR(3),

    study_start_date DATE,
    study_end_date DATE,
    award_date DATE,

    qualification_number VARCHAR(160),
    grade_or_classification VARCHAR(120),

    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    eds_document_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_ewf_qualification_type
        CHECK (
            qualification_type IN (
                'DOCTORATE',
                'MASTERS',
                'BACHELORS',
                'DIPLOMA',
                'CERTIFICATE',
                'VOCATIONAL',
                'PROFESSIONAL',
                'SECONDARY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_qualification_dates
        CHECK (
            study_end_date IS NULL
            OR study_start_date IS NULL
            OR study_end_date >= study_start_date
        ),

    CONSTRAINT ck_ewf_qualification_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'EXPIRED'
            )
        ),

    CONSTRAINT ck_ewf_qualification_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_ewf_qualification_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_certification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id) ON DELETE CASCADE,

    certification_type VARCHAR(80) NOT NULL,
    certification_name VARCHAR(250) NOT NULL,
    issuing_authority VARCHAR(250) NOT NULL,

    certification_number VARCHAR(160),
    issued_at DATE,
    expires_at DATE,

    renewable BOOLEAN NOT NULL DEFAULT FALSE,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    eds_document_id UUID,

    certification_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_ewf_certification_dates
        CHECK (
            expires_at IS NULL
            OR issued_at IS NULL
            OR expires_at >= issued_at
        ),

    CONSTRAINT ck_ewf_certification_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'EXPIRED'
            )
        ),

    CONSTRAINT ck_ewf_certification_lifecycle
        CHECK (
            certification_status IN (
                'ACTIVE',
                'EXPIRING',
                'EXPIRED',
                'SUSPENDED',
                'REVOKED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_ewf_certification_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_skill (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    skill_code VARCHAR(80) NOT NULL,
    skill_name VARCHAR(200) NOT NULL,
    skill_category VARCHAR(100),
    description VARCHAR(1000),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_skill_code
        UNIQUE (tenant_id, skill_code),

    CONSTRAINT ck_ewf_skill_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_workforce_skill (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id) ON DELETE CASCADE,

    skill_id UUID NOT NULL
        REFERENCES ewf_skill(id),

    proficiency_level VARCHAR(30) NOT NULL DEFAULT 'BEGINNER',

    years_of_experience NUMERIC(5,2),
    last_assessed_at TIMESTAMPTZ,
    assessed_by UUID,

    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    eds_evidence_document_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_workforce_skill
        UNIQUE (tenant_id, workforce_member_id, skill_id),

    CONSTRAINT ck_ewf_proficiency_level
        CHECK (
            proficiency_level IN (
                'BEGINNER',
                'INTERMEDIATE',
                'ADVANCED',
                'EXPERT'
            )
        ),

    CONSTRAINT ck_ewf_skill_experience
        CHECK (
            years_of_experience IS NULL
            OR years_of_experience >= 0
        ),

    CONSTRAINT ck_ewf_workforce_skill_verification
        CHECK (
            verified = FALSE
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_ewf_workforce_skill_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_emergency_contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id) ON DELETE CASCADE,

    contact_name VARCHAR(200) NOT NULL,
    relationship_type VARCHAR(80) NOT NULL,
    phone_number VARCHAR(40) NOT NULL,
    alternative_phone_number VARCHAR(40),
    email VARCHAR(200),
    physical_address VARCHAR(500),

    priority_number INTEGER NOT NULL DEFAULT 1,
    authorized_for_emergency_decisions BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_ewf_emergency_contact_priority
        UNIQUE (tenant_id, workforce_member_id, priority_number),

    CONSTRAINT ck_ewf_emergency_contact_priority
        CHECK (priority_number > 0),

    CONSTRAINT ck_ewf_emergency_contact_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_workforce_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id) ON DELETE CASCADE,

    document_type VARCHAR(60) NOT NULL,
    eds_document_id UUID NOT NULL,

    document_reference VARCHAR(160),
    issued_at DATE,
    expires_at DATE,

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

    CONSTRAINT uq_ewf_workforce_document
        UNIQUE (
            tenant_id,
            workforce_member_id,
            document_type,
            eds_document_id
        ),

    CONSTRAINT ck_ewf_workforce_document_type
        CHECK (
            document_type IN (
                'EMPLOYMENT_CONTRACT',
                'APPOINTMENT_LETTER',
                'NATIONAL_ID',
                'PASSPORT',
                'CURRICULUM_VITAE',
                'QUALIFICATION',
                'PROFESSIONAL_LICENCE',
                'MEDICAL_CLEARANCE',
                'POLICE_CLEARANCE',
                'PERFORMANCE_RECORD',
                'EXIT_DOCUMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_ewf_workforce_document_dates
        CHECK (
            expires_at IS NULL
            OR issued_at IS NULL
            OR expires_at >= issued_at
        ),

    CONSTRAINT ck_ewf_document_verification
        CHECK (
            verification_status IN (
                'UNVERIFIED',
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'EXPIRED'
            )
        ),

    CONSTRAINT ck_ewf_workforce_document_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE ewf_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    workforce_member_id UUID NOT NULL
        REFERENCES ewf_workforce_member(id) ON DELETE CASCADE,

    event_type VARCHAR(40) NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30),

    event_reason VARCHAR(1500),
    effective_at TIMESTAMPTZ NOT NULL,
    event_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_ewf_status_history_event
        CHECK (
            event_type IN (
                'RECRUITED',
                'APPOINTED',
                'ONBOARDED',
                'CONFIRMED',
                'PROMOTED',
                'TRANSFERRED',
                'ACTING_APPOINTMENT',
                'LEAVE_STARTED',
                'LEAVE_ENDED',
                'SUSPENDED',
                'REINSTATED',
                'RESIGNED',
                'RETIRED',
                'TERMINATED',
                'CONTRACT_EXPIRED',
                'DECEASED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_ewf_workforce_member_name
    ON ewf_workforce_member (tenant_id, last_name, first_name);

CREATE INDEX ix_ewf_workforce_member_status
    ON ewf_workforce_member (tenant_id, workforce_status);

CREATE INDEX ix_ewf_workforce_member_eiam
    ON ewf_workforce_member (tenant_id, eiam_user_id)
    WHERE eiam_user_id IS NOT NULL;

CREATE INDEX ix_ewf_organizational_unit_parent
    ON ewf_organizational_unit (tenant_id, parent_unit_id);

CREATE INDEX ix_ewf_organizational_unit_type
    ON ewf_organizational_unit (tenant_id, unit_type, unit_status);

CREATE INDEX ix_ewf_position_unit
    ON ewf_position (tenant_id, organizational_unit_id, position_status);

CREATE INDEX ix_ewf_employment_member
    ON ewf_employment (tenant_id, workforce_member_id, employment_status);

CREATE INDEX ix_ewf_assignment_member
    ON ewf_assignment (tenant_id, workforce_member_id, assignment_status);

CREATE INDEX ix_ewf_assignment_position
    ON ewf_assignment (tenant_id, position_id, assignment_status);

CREATE INDEX ix_ewf_qualification_member
    ON ewf_qualification (tenant_id, workforce_member_id);

CREATE INDEX ix_ewf_certification_expiry
    ON ewf_certification (tenant_id, expires_at, certification_status);

CREATE INDEX ix_ewf_workforce_skill_member
    ON ewf_workforce_skill (tenant_id, workforce_member_id);

CREATE INDEX ix_ewf_emergency_contact_member
    ON ewf_emergency_contact (
        tenant_id,
        workforce_member_id,
        priority_number
    );

CREATE INDEX ix_ewf_workforce_document_member
    ON ewf_workforce_document (
        tenant_id,
        workforce_member_id,
        document_type
    );

CREATE INDEX ix_ewf_status_history_member
    ON ewf_status_history (
        tenant_id,
        workforce_member_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '036',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
