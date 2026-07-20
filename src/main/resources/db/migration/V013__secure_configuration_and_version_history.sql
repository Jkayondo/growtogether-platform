ALTER TABLE ecs_configuration_values
    ADD COLUMN encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN encryption_iv VARCHAR(100),
    ADD COLUMN encryption_key_id VARCHAR(100),
    ADD COLUMN value_hash VARCHAR(64);

UPDATE ecs_configuration_values
SET value_hash = encode(digest(stored_value, 'sha256'), 'hex')
WHERE value_hash IS NULL;

ALTER TABLE ecs_configuration_values
    ALTER COLUMN value_hash SET NOT NULL;

CREATE TABLE ecs_configuration_versions (
    id UUID PRIMARY KEY,
    configuration_value_id UUID NOT NULL REFERENCES ecs_configuration_values(id),
    definition_id UUID NOT NULL REFERENCES ecs_configuration_definitions(id),
    version_number BIGINT NOT NULL,
    scope VARCHAR(20) NOT NULL,
    country_code VARCHAR(2),
    organization_id UUID,
    tenant_id UUID,
    stored_value TEXT,
    encrypted BOOLEAN NOT NULL,
    encryption_iv VARCHAR(100),
    encryption_key_id VARCHAR(100),
    value_hash VARCHAR(64) NOT NULL,
    change_reason VARCHAR(500),
    changed_by VARCHAR(160) NOT NULL,
    correlation_id VARCHAR(100),
    rollback_from_version BIGINT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_ecs_configuration_version UNIQUE(configuration_value_id, version_number)
);

CREATE INDEX ix_ecs_version_value ON ecs_configuration_versions(configuration_value_id, version_number DESC);
CREATE INDEX ix_ecs_version_created ON ecs_configuration_versions(created_at DESC);

REVOKE UPDATE, DELETE ON ecs_configuration_versions FROM PUBLIC;
