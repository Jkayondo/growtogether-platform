CREATE TABLE ai_governance_policies (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    policy_code VARCHAR(100) NOT NULL,
    policy_name VARCHAR(200) NOT NULL,

    maximum_risk_level VARCHAR(20) NOT NULL,

    approval_required BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,

    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL,

    CONSTRAINT uq_ai_governance_policy_code
        UNIQUE(tenant_id, policy_code)
);


CREATE INDEX ix_ai_governance_policy_risk
    ON ai_governance_policies(
        tenant_id,
        maximum_risk_level
    );


CREATE INDEX ix_ai_governance_policy_active
    ON ai_governance_policies(
        tenant_id,
        active
    );
