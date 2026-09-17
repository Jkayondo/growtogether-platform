-- GT Executive Intelligence Link Foundation

CREATE TABLE IF NOT EXISTS executive_intelligence_links (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    institution_id UUID NOT NULL,

    domain VARCHAR(100),

    reference_description VARCHAR(3000),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_executive_intelligence_links_tenant
ON executive_intelligence_links(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_executive_intelligence_links_institution
ON executive_intelligence_links(
    tenant_id,
    institution_id
);

