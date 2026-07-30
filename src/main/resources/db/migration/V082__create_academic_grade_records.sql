CREATE TABLE IF NOT EXISTS academic_grade_records (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    subject_configuration_id UUID NOT NULL,

    grade_scale VARCHAR(30) NOT NULL,

    score INTEGER NOT NULL,

    grade_value VARCHAR(20),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_academic_grade_record_tenant
ON academic_grade_records (tenant_id);
