CREATE TABLE eiam_user_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_eiam_user_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT uq_eiam_user_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT ck_eiam_user_account_status CHECK (account_status IN ('PENDING','ACTIVE','SUSPENDED','DEACTIVATED','LOCKED')),
    CONSTRAINT ck_eiam_user_entity_status CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED'))
);
CREATE INDEX ix_eiam_user_tenant ON eiam_user_account (tenant_id);
CREATE INDEX ix_eiam_user_tenant_status ON eiam_user_account (tenant_id, account_status);
UPDATE platform_metadata SET metadata_value = '003', updated_at = CURRENT_TIMESTAMP WHERE metadata_key = 'schema.version';
