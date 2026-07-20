CREATE TABLE eiam_authorization_policy (
 id UUID PRIMARY KEY,
 tenant_id UUID NOT NULL,
 code VARCHAR(100) NOT NULL,
 name VARCHAR(150) NOT NULL,
 description VARCHAR(500),
 resource_type VARCHAR(120) NOT NULL,
 action VARCHAR(120) NOT NULL,
 effect VARCHAR(10) NOT NULL CHECK (effect IN ('ALLOW','DENY')),
 priority INTEGER NOT NULL DEFAULT 0,
 required_permission VARCHAR(180),
 required_role VARCHAR(100),
 owner_only BOOLEAN NOT NULL DEFAULT FALSE,
 minimum_aal INTEGER NOT NULL DEFAULT 1 CHECK (minimum_aal >= 1),
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMPTZ NOT NULL,
 created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 updated_by VARCHAR(150) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uq_eiam_authorization_policy_code UNIQUE (tenant_id, code)
);
CREATE INDEX ix_eiam_authorization_policy_evaluation ON eiam_authorization_policy(tenant_id, resource_type, action, active, priority DESC);
CREATE INDEX ix_eiam_authorization_policy_permission ON eiam_authorization_policy(tenant_id, required_permission) WHERE required_permission IS NOT NULL;
