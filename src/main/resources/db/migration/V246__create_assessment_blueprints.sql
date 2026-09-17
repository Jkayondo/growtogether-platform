-- GT Assessment AI Blueprint Foundation

CREATE TABLE IF NOT EXISTS assessment_blueprints (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    subject_name VARCHAR(200),

    assessment_name VARCHAR(500),

    question_count INTEGER,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_assessment_blueprints_tenant
ON assessment_blueprints(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_assessment_blueprints_subject
ON assessment_blueprints(
    tenant_id,
    subject_name
);

