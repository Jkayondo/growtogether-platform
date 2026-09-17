-- GT Academic Risk Intelligence Foundation

CREATE TABLE IF NOT EXISTS academic_risk_indicators (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    indicator_code VARCHAR(100) NOT NULL,

    indicator_name VARCHAR(250) NOT NULL,

    severity VARCHAR(50),

    indicator_status VARCHAR(50)
        NOT NULL DEFAULT 'ACTIVE',

    description VARCHAR(1500),

    detected_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_academic_risk_indicator_code
        UNIQUE (
            tenant_id,
            learner_id,
            indicator_code
        )
);


CREATE INDEX IF NOT EXISTS ix_academic_risk_indicator_tenant
ON academic_risk_indicators(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_academic_risk_indicator_learner
ON academic_risk_indicators(
    tenant_id,
    learner_id
);
