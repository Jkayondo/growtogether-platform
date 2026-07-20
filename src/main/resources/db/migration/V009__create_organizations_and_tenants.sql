CREATE TABLE eiam_organization (
 id UUID PRIMARY KEY, code VARCHAR(80) NOT NULL UNIQUE, name VARCHAR(200) NOT NULL, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE eiam_tenant (
 id UUID PRIMARY KEY, organization_id UUID NOT NULL REFERENCES eiam_organization(id), code VARCHAR(80) NOT NULL UNIQUE,
 name VARCHAR(200) NOT NULL, status VARCHAR(20) NOT NULL, created_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_eiam_tenant_status CHECK (status IN ('PROVISIONING','ACTIVE','SUSPENDED','DEACTIVATED'))
);
CREATE INDEX idx_eiam_tenant_organization ON eiam_tenant(organization_id);
CREATE INDEX idx_eiam_tenant_status ON eiam_tenant(status);
UPDATE platform_metadata SET metadata_value = '009', updated_at = CURRENT_TIMESTAMP WHERE metadata_key = 'schema.version';
