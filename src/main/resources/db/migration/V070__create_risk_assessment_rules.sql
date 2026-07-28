CREATE TABLE gts_risk_assessment_rule (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    rule_name VARCHAR(150) NOT NULL,

    minimum_score DOUBLE PRECISION,

    maximum_score DOUBLE PRECISION,

    risk_level VARCHAR(50) NOT NULL,

    support_required BOOLEAN DEFAULT FALSE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);
