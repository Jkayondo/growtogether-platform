CREATE TABLE ecs_configuration_definitions (
 id uuid PRIMARY KEY, code varchar(120) NOT NULL, name varchar(160) NOT NULL, category varchar(80) NOT NULL, description varchar(1000), data_type varchar(20) NOT NULL, default_value text, validation_rules jsonb NOT NULL DEFAULT '{}'::jsonb, allowed_scopes text[] NOT NULL, required boolean NOT NULL DEFAULT false, secret_value boolean NOT NULL DEFAULT false, active boolean NOT NULL DEFAULT true, version bigint NOT NULL DEFAULT 0, created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL, CONSTRAINT uk_ecs_definition_code UNIQUE(code));
CREATE TABLE ecs_configuration_values (
 id uuid PRIMARY KEY, definition_id uuid NOT NULL REFERENCES ecs_configuration_definitions(id), scope varchar(20) NOT NULL, country_code varchar(2), organization_id uuid, tenant_id uuid, stored_value text NOT NULL, change_reason varchar(500), active boolean NOT NULL DEFAULT true, version bigint NOT NULL DEFAULT 0, created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL);
CREATE UNIQUE INDEX uk_ecs_value_platform ON ecs_configuration_values(definition_id) WHERE scope='PLATFORM' AND active;
CREATE UNIQUE INDEX uk_ecs_value_country ON ecs_configuration_values(definition_id,country_code) WHERE scope='COUNTRY' AND active;
CREATE UNIQUE INDEX uk_ecs_value_organization ON ecs_configuration_values(definition_id,organization_id) WHERE scope='ORGANIZATION' AND active;
CREATE UNIQUE INDEX uk_ecs_value_tenant ON ecs_configuration_values(definition_id,tenant_id) WHERE scope='TENANT' AND active;
CREATE INDEX idx_ecs_definition_category ON ecs_configuration_definitions(category,code);
CREATE INDEX idx_ecs_value_resolution ON ecs_configuration_values(definition_id,scope,tenant_id,organization_id,country_code) WHERE active;
