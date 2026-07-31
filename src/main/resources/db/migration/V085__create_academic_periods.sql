CREATE TABLE IF NOT EXISTS academic_periods (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    period_name VARCHAR(100) NOT NULL,

    period_type VARCHAR(30) NOT NULL,

    start_date DATE NOT NULL,

    end_date DATE NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_academic_period_tenant
ON academic_periods (tenant_id);
