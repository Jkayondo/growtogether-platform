CREATE TABLE eds_documents (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_number VARCHAR(80) NOT NULL, title VARCHAR(240) NOT NULL, description TEXT,
 document_status VARCHAR(24) NOT NULL, classification VARCHAR(24) NOT NULL, current_version INT NOT NULL DEFAULT 0,
 retention_until TIMESTAMPTZ, legal_hold BOOLEAN NOT NULL DEFAULT FALSE, checked_out_by UUID, checked_out_at TIMESTAMPTZ,
 archived_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL,
 CONSTRAINT uk_eds_document_tenant_number UNIQUE(tenant_id,document_number));
CREATE TABLE eds_document_versions (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_id UUID NOT NULL REFERENCES eds_documents(id), version_number INT NOT NULL,
 storage_key VARCHAR(500) NOT NULL UNIQUE, checksum VARCHAR(128) NOT NULL, mime_type VARCHAR(150) NOT NULL, size_bytes BIGINT NOT NULL,
 change_summary VARCHAR(500), immutable BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL,
 CONSTRAINT uk_eds_document_version UNIQUE(tenant_id,document_id,version_number));
CREATE INDEX ix_eds_version_checksum ON eds_document_versions(tenant_id,checksum);
CREATE TABLE eds_document_lifecycle_events (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_id UUID NOT NULL REFERENCES eds_documents(id), event_type VARCHAR(50) NOT NULL,
 details TEXT, occurred_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL);
CREATE INDEX ix_eds_event_document ON eds_document_lifecycle_events(tenant_id,document_id,occurred_at DESC);
REVOKE UPDATE, DELETE ON eds_document_versions, eds_document_lifecycle_events FROM PUBLIC;
