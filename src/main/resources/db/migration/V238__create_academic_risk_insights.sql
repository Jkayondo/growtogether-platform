-- GT Academic Risk Intelligence Insight Foundation

CREATE TABLE IF NOT EXISTS academic_risk_insights (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    learner_id UUID NOT NULL,

    subject_id UUID,

    subject_name VARCHAR(200),

    insight_type VARCHAR(100) NOT NULL,

    severity VARCHAR(50),

    insight_description VARCHAR(2000),

    recommended_action VARCHAR(2000),

    detected_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_academic_risk_insights_tenant
ON academic_risk_insights(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_academic_risk_insights_learner
ON academic_risk_insights(
    tenant_id,
    learner_id
);

