CREATE TABLE IF NOT EXISTS school_configurations (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    school_name VARCHAR(200) NOT NULL,

    registration_number VARCHAR(100),

    country_code VARCHAR(10) NOT NULL,

    region VARCHAR(100),

    school_type VARCHAR(50) NOT NULL,

    ownership_type VARCHAR(50),

    logo_document_id UUID,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_school_configuration_tenant
ON school_configurations (tenant_id);
