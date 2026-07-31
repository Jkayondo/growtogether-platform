CREATE TABLE IF NOT EXISTS subject_configurations (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    academic_grade_id UUID NOT NULL,

    subject_name VARCHAR(150) NOT NULL,

    subject_code VARCHAR(50),

    mandatory BOOLEAN NOT NULL,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_subject_configuration_tenant
ON subject_configurations (tenant_id);
