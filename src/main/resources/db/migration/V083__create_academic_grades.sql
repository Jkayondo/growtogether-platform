CREATE TABLE IF NOT EXISTS academic_grades (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    academic_level_id UUID NOT NULL,

    grade_name VARCHAR(100) NOT NULL,

    display_order INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);


CREATE INDEX IF NOT EXISTS ix_academic_grade_tenant
ON academic_grades (tenant_id);
