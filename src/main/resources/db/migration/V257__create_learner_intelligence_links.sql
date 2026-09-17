-- GT Learner Intelligence Link Foundation

CREATE TABLE IF NOT EXISTS learner_intelligence_links (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    domain VARCHAR(100),

    reference_description VARCHAR(3000),

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_learner_intelligence_links_tenant
ON learner_intelligence_links(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_learner_intelligence_links_learner
ON learner_intelligence_links(
    tenant_id,
    learner_id
);
