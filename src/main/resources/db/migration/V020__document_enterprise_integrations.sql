CREATE TABLE eds_workflow_document_links (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, workflow_instance_id UUID NOT NULL, document_id UUID NOT NULL,
 relationship_type VARCHAR(40) NOT NULL, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(255), updated_at TIMESTAMPTZ, updated_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0, status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT fk_eds_workflow_link_document FOREIGN KEY(document_id) REFERENCES eds_documents(id),
 CONSTRAINT uk_eds_workflow_document_link UNIQUE(tenant_id,workflow_instance_id,document_id,relationship_type)
);
CREATE INDEX idx_eds_workflow_links_instance ON eds_workflow_document_links(tenant_id,workflow_instance_id);
CREATE TABLE eds_document_event_outbox (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_id UUID NOT NULL, event_type VARCHAR(100) NOT NULL,
 payload_json TEXT NOT NULL, published_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(255), updated_at TIMESTAMPTZ, updated_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0, status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT fk_eds_outbox_document FOREIGN KEY(document_id) REFERENCES eds_documents(id)
);
CREATE INDEX idx_eds_outbox_unpublished ON eds_document_event_outbox(published_at,created_at);
CREATE TABLE eds_document_ai_requests (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, document_id UUID NOT NULL, operation VARCHAR(50) NOT NULL, request_status VARCHAR(24) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(255), updated_at TIMESTAMPTZ, updated_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0, status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT fk_eds_ai_document FOREIGN KEY(document_id) REFERENCES eds_documents(id)
);
CREATE INDEX idx_eds_ai_request_document ON eds_document_ai_requests(tenant_id,document_id,created_at DESC);
