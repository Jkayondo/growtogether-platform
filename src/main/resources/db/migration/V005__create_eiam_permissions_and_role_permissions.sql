CREATE TABLE eiam_permission (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(150) NOT NULL,
    name VARCHAR(150) NOT NULL,
    module VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    system_permission BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_eiam_permission_tenant_code UNIQUE (tenant_id, code)
);
CREATE INDEX ix_eiam_permission_tenant_module ON eiam_permission (tenant_id, module, code);

CREATE TABLE eiam_role_permission (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    role_id UUID NOT NULL REFERENCES eiam_role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES eiam_permission(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_eiam_role_permission_assignment UNIQUE (tenant_id, role_id, permission_id)
);
CREATE INDEX ix_eiam_role_permission_role ON eiam_role_permission (tenant_id, role_id);
CREATE INDEX ix_eiam_role_permission_permission ON eiam_role_permission (tenant_id, permission_id);
