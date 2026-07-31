CREATE TABLE IF NOT EXISTS school_onboarding_workflows (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    school_configuration_id UUID NOT NULL,

    onboarding_status VARCHAR(50) NOT NULL,

    started_at TIMESTAMP NOT NULL,

    completed_at TIMESTAMP,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_school_onboarding_tenant
ON school_onboarding_workflows (tenant_id);
