-- GT Learner AI Experience Context Foundation

CREATE TABLE IF NOT EXISTS learner_ai_experience_context (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    experience_summary VARCHAR(10000),

    experience_status VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_learner_ai_experience_context_tenant
ON learner_ai_experience_context(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_learner_ai_experience_context_learner
ON learner_ai_experience_context(
    tenant_id,
    learner_id
);

