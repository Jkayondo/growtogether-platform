CREATE TABLE gts_school_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    school_code VARCHAR(80) NOT NULL,
    school_name VARCHAR(200) NOT NULL,
    legal_name VARCHAR(250),
    education_system VARCHAR(100),
    country_code VARCHAR(2),
    default_currency VARCHAR(3),
    timezone VARCHAR(80),
    email VARCHAR(200),
    phone_number VARCHAR(40),
    website VARCHAR(250),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_school_profile_tenant
        UNIQUE (tenant_id),

    CONSTRAINT uq_gts_school_profile_tenant_code
        UNIQUE (tenant_id, school_code),

    CONSTRAINT ck_gts_school_profile_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

CREATE TABLE gts_campus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    school_profile_id UUID NOT NULL REFERENCES gts_school_profile(id),
    campus_code VARCHAR(80) NOT NULL,
    campus_name VARCHAR(200) NOT NULL,
    address_line VARCHAR(300),
    district VARCHAR(120),
    city VARCHAR(120),
    country_code VARCHAR(2),
    phone_number VARCHAR(40),
    email VARCHAR(200),
    main_campus BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_campus_tenant_code
        UNIQUE (tenant_id, campus_code),

    CONSTRAINT ck_gts_campus_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_campus_main
    ON gts_campus (tenant_id)
    WHERE main_campus = TRUE AND status = 'ACTIVE';

CREATE TABLE gts_academic_year (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    academic_year_code VARCHAR(40) NOT NULL,
    academic_year_name VARCHAR(120) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    current_year BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_year_tenant_code
        UNIQUE (tenant_id, academic_year_code),

    CONSTRAINT ck_gts_academic_year_dates
        CHECK (end_date > start_date),

    CONSTRAINT ck_gts_academic_year_status
        CHECK (status IN ('PLANNED', 'ACTIVE', 'CLOSED', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_academic_year_current
    ON gts_academic_year (tenant_id)
    WHERE current_year = TRUE AND status = 'ACTIVE';

CREATE TABLE gts_academic_term (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    academic_year_id UUID NOT NULL REFERENCES gts_academic_year(id),
    term_code VARCHAR(40) NOT NULL,
    term_name VARCHAR(120) NOT NULL,
    sequence_number INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    current_term BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_term_year_code
        UNIQUE (tenant_id, academic_year_id, term_code),

    CONSTRAINT uq_gts_academic_term_year_sequence
        UNIQUE (tenant_id, academic_year_id, sequence_number),

    CONSTRAINT ck_gts_academic_term_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_academic_term_dates
        CHECK (end_date > start_date),

    CONSTRAINT ck_gts_academic_term_status
        CHECK (status IN ('PLANNED', 'ACTIVE', 'CLOSED', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_gts_academic_term_current
    ON gts_academic_term (tenant_id)
    WHERE current_term = TRUE AND status = 'ACTIVE';

CREATE TABLE gts_education_level (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    level_code VARCHAR(60) NOT NULL,
    level_name VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    sequence_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_education_level_tenant_code
        UNIQUE (tenant_id, level_code),

    CONSTRAINT uq_gts_education_level_tenant_sequence
        UNIQUE (tenant_id, sequence_number),

    CONSTRAINT ck_gts_education_level_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_education_level_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_class_grade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    education_level_id UUID NOT NULL REFERENCES gts_education_level(id),
    class_code VARCHAR(60) NOT NULL,
    class_name VARCHAR(160) NOT NULL,
    sequence_number INTEGER NOT NULL,
    capacity INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_class_grade_tenant_code
        UNIQUE (tenant_id, class_code),

    CONSTRAINT uq_gts_class_grade_level_sequence
        UNIQUE (tenant_id, education_level_id, sequence_number),

    CONSTRAINT ck_gts_class_grade_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_class_grade_capacity
        CHECK (capacity IS NULL OR capacity > 0),

    CONSTRAINT ck_gts_class_grade_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_stream (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    campus_id UUID NOT NULL REFERENCES gts_campus(id),
    class_grade_id UUID NOT NULL REFERENCES gts_class_grade(id),
    stream_code VARCHAR(60) NOT NULL,
    stream_name VARCHAR(160) NOT NULL,
    capacity INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_stream_class_campus_code
        UNIQUE (tenant_id, campus_id, class_grade_id, stream_code),

    CONSTRAINT ck_gts_stream_capacity
        CHECK (capacity IS NULL OR capacity > 0),

    CONSTRAINT ck_gts_stream_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_subject (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),
    subject_code VARCHAR(60) NOT NULL,
    subject_name VARCHAR(180) NOT NULL,
    short_name VARCHAR(80),
    subject_type VARCHAR(30) NOT NULL DEFAULT 'CORE',
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_subject_tenant_code
        UNIQUE (tenant_id, subject_code),

    CONSTRAINT ck_gts_subject_type
        CHECK (subject_type IN ('CORE', 'ELECTIVE', 'VOCATIONAL', 'CO_CURRICULAR')),

    CONSTRAINT ck_gts_subject_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE INDEX ix_gts_school_profile_status
    ON gts_school_profile (tenant_id, status);

CREATE INDEX ix_gts_campus_school
    ON gts_campus (tenant_id, school_profile_id, status);

CREATE INDEX ix_gts_academic_year_dates
    ON gts_academic_year (tenant_id, start_date, end_date);

CREATE INDEX ix_gts_academic_term_year
    ON gts_academic_term (tenant_id, academic_year_id, sequence_number);

CREATE INDEX ix_gts_education_level_sequence
    ON gts_education_level (tenant_id, sequence_number);

CREATE INDEX ix_gts_class_grade_level
    ON gts_class_grade (tenant_id, education_level_id, sequence_number);

CREATE INDEX ix_gts_stream_class
    ON gts_stream (tenant_id, class_grade_id, campus_id);

CREATE INDEX ix_gts_subject_name
    ON gts_subject (tenant_id, subject_name);

UPDATE platform_metadata
SET metadata_value = '032',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
