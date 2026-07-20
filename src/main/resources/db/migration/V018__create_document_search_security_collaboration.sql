CREATE TABLE eds_document_shares (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_id UUID NOT NULL,
 recipient_user_id UUID, recipient_email VARCHAR(320), access_level VARCHAR(16) NOT NULL,
 token_hash VARCHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL,
 revoked_at TIMESTAMPTZ, max_downloads INTEGER, download_count INTEGER NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(255) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(255) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT fk_eds_share_document FOREIGN KEY(document_id) REFERENCES eds_documents(id)
);
CREATE INDEX ix_eds_share_document ON eds_document_shares(tenant_id,document_id);
CREATE INDEX ix_eds_share_expiry ON eds_document_shares(tenant_id,expires_at) WHERE revoked_at IS NULL;
CREATE TABLE eds_document_collaboration_events (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_id UUID NOT NULL,
 event_type VARCHAR(48) NOT NULL, actor_user_id UUID, details TEXT, occurred_at TIMESTAMPTZ NOT NULL,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(255) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(255) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT fk_eds_collab_document FOREIGN KEY(document_id) REFERENCES eds_documents(id)
);
CREATE INDEX ix_eds_collab_document ON eds_document_collaboration_events(tenant_id,document_id,occurred_at DESC);
CREATE INDEX ix_eds_document_search ON eds_documents USING gin(to_tsvector('simple',coalesce(title,'')||' '||coalesce(description,'')||' '||coalesce(document_number,'')));
REVOKE UPDATE, DELETE ON eds_document_collaboration_events FROM PUBLIC;
