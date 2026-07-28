CREATE TABLE gts_performance_rule (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    rule_name VARCHAR(150) NOT NULL,

    minimum_score DOUBLE PRECISION,

    maximum_score DOUBLE PRECISION,

    achievement_status VARCHAR(50),

    risk_level VARCHAR(50),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);
