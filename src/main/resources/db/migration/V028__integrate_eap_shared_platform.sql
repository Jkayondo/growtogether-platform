-- Shared-platform integration indexes for analytics ingestion, schedules, alerts and source governance.
CREATE INDEX IF NOT EXISTS idx_eap_events_source_time
    ON eap_events (tenant_id, source_service, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_eap_events_correlation
    ON eap_events (tenant_id, correlation_id)
    WHERE correlation_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_eap_alerts_status_severity
    ON eap_alerts (tenant_id, status, severity, triggered_at DESC);
CREATE INDEX IF NOT EXISTS idx_eap_report_schedules_due
    ON eap_report_schedules (tenant_id, status, next_run_at)
    WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_eap_data_sources_type_status
    ON eap_data_sources (tenant_id, source_type, active);
