CREATE TABLE ewe_workflow_definitions (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, code VARCHAR(120) NOT NULL, name VARCHAR(180) NOT NULL,
 category VARCHAR(80) NOT NULL, description VARCHAR(1000), definition_status VARCHAR(20) NOT NULL,
 active_version INTEGER, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uk_ewe_definition_tenant_code UNIQUE (tenant_id, code),
 CONSTRAINT ck_ewe_definition_status CHECK (definition_status IN ('DRAFT','ACTIVE','DEPRECATED','ARCHIVED'))
);
CREATE INDEX ix_ewe_definition_tenant_category ON ewe_workflow_definitions(tenant_id, category);

CREATE TABLE ewe_workflow_versions (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, definition_id UUID NOT NULL REFERENCES ewe_workflow_definitions(id),
 version_number INTEGER NOT NULL, definition_json JSONB NOT NULL, checksum VARCHAR(64) NOT NULL, published BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
 updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uk_ewe_version_definition_number UNIQUE (tenant_id, definition_id, version_number),
 CONSTRAINT ck_ewe_version_positive CHECK (version_number > 0)
);
CREATE INDEX ix_ewe_version_definition ON ewe_workflow_versions(tenant_id, definition_id, version_number DESC);

CREATE TABLE ewe_workflow_instances (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, definition_id UUID NOT NULL REFERENCES ewe_workflow_definitions(id),
 definition_version INTEGER NOT NULL, business_key VARCHAR(180), instance_status VARCHAR(20) NOT NULL,
 current_step VARCHAR(160), execution_context JSONB NOT NULL DEFAULT '{}'::jsonb, started_at TIMESTAMPTZ,
 completed_at TIMESTAMPTZ, failure_reason VARCHAR(1000), restart_of_instance_id UUID REFERENCES ewe_workflow_instances(id),
 retry_count INTEGER NOT NULL DEFAULT 0, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT ck_ewe_instance_status CHECK (instance_status IN ('CREATED','RUNNING','WAITING','COMPLETED','FAILED','CANCELLED')),
 CONSTRAINT ck_ewe_retry_nonnegative CHECK (retry_count >= 0)
);
CREATE INDEX ix_ewe_instance_tenant_status ON ewe_workflow_instances(tenant_id, instance_status);
CREATE INDEX ix_ewe_instance_business_key ON ewe_workflow_instances(tenant_id, business_key);
CREATE INDEX ix_ewe_instance_definition ON ewe_workflow_instances(tenant_id, definition_id, definition_version);

CREATE TABLE ewe_workflow_variables (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, instance_id UUID NOT NULL REFERENCES ewe_workflow_instances(id) ON DELETE CASCADE,
 variable_key VARCHAR(160) NOT NULL, variable_value JSONB, sensitive BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
 updated_by VARCHAR(150) NOT NULL, version BIGINT NOT NULL DEFAULT 0, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uk_ewe_variable_instance_key UNIQUE (tenant_id, instance_id, variable_key)
);
CREATE INDEX ix_ewe_variable_instance ON ewe_workflow_variables(tenant_id, instance_id);

CREATE TABLE ewe_workflow_execution_events (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, instance_id UUID NOT NULL REFERENCES ewe_workflow_instances(id) ON DELETE CASCADE,
 event_type VARCHAR(30) NOT NULL, step_code VARCHAR(160), message VARCHAR(1000), details JSONB NOT NULL DEFAULT '{}'::jsonb,
 occurred_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_ewe_event_type CHECK (event_type IN ('STARTED','STEP_ADVANCED','WAITING','RESUMED','COMPLETED','FAILED','RETRIED','CANCELLED','RESTARTED','VARIABLE_SET'))
);
CREATE INDEX ix_ewe_event_instance_time ON ewe_workflow_execution_events(tenant_id, instance_id, occurred_at);

REVOKE UPDATE, DELETE ON ewe_workflow_execution_events FROM PUBLIC;
