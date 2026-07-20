CREATE TABLE eiam_role (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_eiam_role_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_eiam_role_tenant_name UNIQUE (tenant_id, name)
);
CREATE INDEX ix_eiam_role_tenant ON eiam_role (tenant_id);

CREATE TABLE eiam_user_role (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES eiam_user_account(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES eiam_role(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_eiam_user_role_assignment UNIQUE (tenant_id, user_id, role_id)
);
CREATE INDEX ix_eiam_user_role_user ON eiam_user_role (tenant_id, user_id);
CREATE INDEX ix_eiam_user_role_role ON eiam_user_role (tenant_id, role_id);
