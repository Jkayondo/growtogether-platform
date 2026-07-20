CREATE INDEX IF NOT EXISTS ix_eaif_requests_model_status ON eaif_requests (tenant_id, model_code, request_status);
CREATE INDEX IF NOT EXISTS ix_eaif_requests_risk_status ON eaif_requests (tenant_id, risk_level, request_status);
CREATE INDEX IF NOT EXISTS ix_eaif_requests_completed ON eaif_requests (tenant_id, completed_at) WHERE completed_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_eaif_models_provider_capability ON eaif_models (tenant_id, provider_code, capability);
