CREATE TABLE platform_tenant_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    record_key VARCHAR(100) NOT NULL,
    record_value VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT ck_platform_tenant_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    CONSTRAINT uq_platform_tenant_record_tenant_key
        UNIQUE (tenant_id, record_key)
);

CREATE INDEX ix_platform_tenant_record_tenant
    ON platform_tenant_record (tenant_id);

UPDATE platform_metadata
SET metadata_value = '002', updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
