CREATE TABLE eip_connector_certifications (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, connector_id UUID NOT NULL,
 environment VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL,
 certified_at TIMESTAMPTZ, expires_at TIMESTAMPTZ,
 evidence_reference VARCHAR(500), notes TEXT,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(255) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(255) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0, entity_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT fk_eip_cert_connector FOREIGN KEY(connector_id) REFERENCES eip_external_connectors(id),
 CONSTRAINT uk_eip_connector_cert UNIQUE(tenant_id,connector_id,environment)
);
CREATE INDEX idx_eip_cert_tenant_status ON eip_connector_certifications(tenant_id,status);
CREATE INDEX idx_eip_message_ops ON eip_messages(tenant_id,status,next_attempt_at);
CREATE INDEX idx_eip_payment_ops ON eip_payment_transactions(tenant_id,status,created_at);
