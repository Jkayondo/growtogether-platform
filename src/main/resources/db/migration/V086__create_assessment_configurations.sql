CREATE TABLE IF NOT EXISTS assessment_configurations (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    subject_configuration_id UUID NOT NULL,

    assessment_type VARCHAR(50) NOT NULL,

    assessment_name VARCHAR(150) NOT NULL,

    weight_percentage INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_assessment_configuration_tenant
ON assessment_configurations (tenant_id);
