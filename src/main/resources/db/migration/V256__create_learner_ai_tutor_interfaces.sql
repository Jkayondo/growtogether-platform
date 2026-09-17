-- GT Learner AI Tutor Interface Foundation

CREATE TABLE IF NOT EXISTS learner_ai_tutor_interfaces (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    interface_name VARCHAR(500),

    status VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_learner_ai_tutor_interfaces_tenant
ON learner_ai_tutor_interfaces(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_learner_ai_tutor_interfaces_learner
ON learner_ai_tutor_interfaces(
    tenant_id,
    learner_id
);

