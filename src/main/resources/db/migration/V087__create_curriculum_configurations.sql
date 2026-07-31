CREATE TABLE IF NOT EXISTS curriculum_configurations (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    school_configuration_id UUID NOT NULL,

    curriculum_type VARCHAR(50) NOT NULL,

    curriculum_name VARCHAR(200) NOT NULL,

    country_code VARCHAR(10),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_curriculum_configuration_tenant
ON curriculum_configurations (tenant_id);
