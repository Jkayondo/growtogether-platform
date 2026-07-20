-- EIP shared-platform integration indexes and certification controls.
CREATE INDEX IF NOT EXISTS idx_eip_messages_tenant_source_created
    ON eip_messages (tenant_id, source_service, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_eip_messages_tenant_destination_status
    ON eip_messages (tenant_id, destination, status);
CREATE INDEX IF NOT EXISTS idx_eip_payment_tenant_provider_reference
    ON eip_payment_transactions (tenant_id, provider_reference)
    WHERE provider_reference IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_eip_certifications_tenant_status_expiry
    ON eip_connector_certifications (tenant_id, status, expires_at);
