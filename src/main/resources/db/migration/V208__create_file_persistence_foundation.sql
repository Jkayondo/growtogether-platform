-- GT-FILE-PERSISTENCE-FOUNDATION-001
--
-- Persistence foundation for the file capability.
-- Platform lifecycle status remains in the inherited "status" column.
-- File-domain security lifecycle is stored separately as "security_status".

CREATE TABLE file_records (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    storage_key VARCHAR(500) NOT NULL,

    checksum VARCHAR(128) NOT NULL,

    mime_type VARCHAR(150) NOT NULL,

    size_bytes BIGINT NOT NULL,

    owner_type VARCHAR(50) NOT NULL,

    owner_id UUID NOT NULL,

    security_status VARCHAR(30) NOT NULL,

    retention_until TIMESTAMPTZ,

    legal_hold BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);


CREATE TABLE file_quarantined_files (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    storage_key VARCHAR(500) NOT NULL,

    checksum VARCHAR(128) NOT NULL,

    mime_type VARCHAR(150) NOT NULL,

    size_bytes BIGINT NOT NULL,

    security_status VARCHAR(30) NOT NULL,

    quarantine_reason VARCHAR(500),

    reviewed_by UUID,

    reviewed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);


CREATE TABLE file_accesses (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    file_id UUID NOT NULL
        REFERENCES file_records(id),

    token_hash VARCHAR(64) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,

    download_count INTEGER NOT NULL DEFAULT 0,

    max_downloads INTEGER,

    revoked_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT uk_file_access_token
        UNIQUE (token_hash)
);


CREATE INDEX ix_file_access_token
    ON file_accesses(token_hash);
