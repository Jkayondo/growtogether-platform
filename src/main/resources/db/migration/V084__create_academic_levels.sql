CREATE TABLE IF NOT EXISTS academic_levels (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    curriculum_configuration_id UUID NOT NULL,

    level_name VARCHAR(100) NOT NULL,

    display_order INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_academic_level_tenant
ON academic_levels (tenant_id);
