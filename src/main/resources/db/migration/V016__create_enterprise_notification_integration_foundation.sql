CREATE TABLE ens_notification_requests (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, definition_code varchar(120) NOT NULL, recipient varchar(320) NOT NULL, channel varchar(20) NOT NULL, priority varchar(20) NOT NULL, notification_status varchar(24) NOT NULL, subject varchar(300), body text NOT NULL, correlation_id varchar(100), source_service varchar(80) NOT NULL, source_reference varchar(160), attempt_count integer NOT NULL DEFAULT 0, next_attempt_at timestamptz, provider_reference varchar(200), last_error text, created_at timestamptz NOT NULL, created_by varchar(150) NOT NULL, updated_at timestamptz NOT NULL, updated_by varchar(150) NOT NULL, version bigint NOT NULL DEFAULT 0, status varchar(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX ix_ens_request_tenant_status ON ens_notification_requests(tenant_id, notification_status);
CREATE INDEX ix_ens_request_correlation ON ens_notification_requests(correlation_id);
CREATE INDEX ix_ens_request_retry ON ens_notification_requests(notification_status, next_attempt_at) WHERE notification_status='RETRYING';
