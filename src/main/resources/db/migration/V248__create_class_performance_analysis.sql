-- GT Class Performance Intelligence Foundation

CREATE TABLE IF NOT EXISTS class_performance_analysis (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    class_id UUID NOT NULL,

    average_score DOUBLE PRECISION,

    performance_status VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_class_performance_analysis_tenant
ON class_performance_analysis(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_class_performance_analysis_class
ON class_performance_analysis(
    tenant_id,
    class_id
);

