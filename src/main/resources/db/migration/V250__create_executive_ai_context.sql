-- GT Executive AI Context Foundation

CREATE TABLE IF NOT EXISTS executive_ai_context (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    institution_id UUID NOT NULL,

    intelligence_summary VARCHAR(10000),

    integration_status VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_executive_ai_context_tenant
ON executive_ai_context(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_executive_ai_context_institution
ON executive_ai_context(
    tenant_id,
    institution_id
);

