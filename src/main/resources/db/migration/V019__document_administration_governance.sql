CREATE TABLE eds_governance_jobs (
 id UUID PRIMARY KEY,
 tenant_id UUID NOT NULL,
 job_type VARCHAR(48) NOT NULL,
 job_status VARCHAR(24) NOT NULL,
 criteria_json JSONB NOT NULL DEFAULT '{}'::jsonb,
 result_json JSONB,
 requested_by UUID,
 requested_at TIMESTAMPTZ NOT NULL,
 completed_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_eds_governance_jobs_tenant_status ON eds_governance_jobs(tenant_id, job_status, requested_at DESC);
CREATE INDEX idx_eds_documents_governance ON eds_documents(tenant_id, document_status, classification, legal_hold, retention_until);
