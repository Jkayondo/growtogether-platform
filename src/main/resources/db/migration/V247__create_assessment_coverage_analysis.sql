-- GT Assessment Coverage Intelligence Foundation

CREATE TABLE IF NOT EXISTS assessment_coverage_analysis (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    subject_name VARCHAR(200),

    topic_coverage_percentage DOUBLE PRECISION,

    coverage_comment VARCHAR(2000),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_assessment_coverage_analysis_tenant
ON assessment_coverage_analysis(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_assessment_coverage_analysis_subject
ON assessment_coverage_analysis(
    tenant_id,
    subject_name
);

