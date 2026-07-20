CREATE TABLE eap_events (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, event_type varchar(160) NOT NULL, source_service varchar(100) NOT NULL,
 correlation_id varchar(100), event_time timestamptz NOT NULL, payload_json text NOT NULL,
 processing_status varchar(30) NOT NULL, attempt_count integer NOT NULL DEFAULT 0, last_error text,
 created_at timestamptz NOT NULL, created_by varchar(255), updated_at timestamptz NOT NULL, updated_by varchar(255), version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL
);
CREATE INDEX ix_eap_event_tenant_status ON eap_events(tenant_id,processing_status,event_time);
CREATE INDEX ix_eap_event_type_time ON eap_events(tenant_id,event_type,event_time);
CREATE INDEX ix_eap_event_source ON eap_events(tenant_id,source_service,event_time);
CREATE TABLE eap_metric_definitions (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, metric_code varchar(120) NOT NULL, metric_name varchar(180) NOT NULL,
 event_type varchar(160) NOT NULL, value_path varchar(300), dimension_paths_json text, metric_type varchar(30) NOT NULL,
 aggregation_period varchar(30) NOT NULL, active boolean NOT NULL DEFAULT true,
 created_at timestamptz NOT NULL, created_by varchar(255), updated_at timestamptz NOT NULL, updated_by varchar(255), version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL,
 CONSTRAINT uq_eap_metric_code UNIQUE(tenant_id,metric_code)
);
CREATE TABLE eap_metric_aggregates (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, metric_code varchar(120) NOT NULL, bucket_start timestamptz NOT NULL, bucket_end timestamptz NOT NULL,
 dimension_hash varchar(64) NOT NULL, dimensions_json text, sample_count bigint NOT NULL DEFAULT 0,
 sum_value numeric(30,8) NOT NULL DEFAULT 0, minimum_value numeric(30,8), maximum_value numeric(30,8),
 created_at timestamptz NOT NULL, created_by varchar(255), updated_at timestamptz NOT NULL, updated_by varchar(255), version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL,
 CONSTRAINT uq_eap_aggregate_bucket UNIQUE(tenant_id,metric_code,bucket_start,dimension_hash)
);
CREATE INDEX ix_eap_aggregate_query ON eap_metric_aggregates(tenant_id,metric_code,bucket_start);
CREATE TABLE eap_dashboard_definitions (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, dashboard_code varchar(120) NOT NULL, dashboard_name varchar(180) NOT NULL,
 widget_configuration_json text NOT NULL, active boolean NOT NULL DEFAULT true,
 created_at timestamptz NOT NULL, created_by varchar(255), updated_at timestamptz NOT NULL, updated_by varchar(255), version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL,
 CONSTRAINT uq_eap_dashboard_code UNIQUE(tenant_id,dashboard_code)
);
CREATE TABLE eap_report_executions (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, report_code varchar(120) NOT NULL, parameters_json text,
 export_format varchar(20) NOT NULL, report_status varchar(20) NOT NULL, requested_at timestamptz NOT NULL,
 completed_at timestamptz, artifact_reference varchar(500), failure_reason text,
 created_at timestamptz NOT NULL, created_by varchar(255), updated_at timestamptz NOT NULL, updated_by varchar(255), version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL
);
CREATE INDEX ix_eap_report_status ON eap_report_executions(tenant_id,report_status,requested_at);
