CREATE TABLE eip_gateway_routes (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, route_code varchar(100) NOT NULL, path_pattern varchar(250) NOT NULL,
 http_method varchar(12) NOT NULL, upstream_uri varchar(500) NOT NULL, required_authority varchar(180),
 rate_limit_per_minute integer NOT NULL, timeout_millis integer NOT NULL, active boolean NOT NULL DEFAULT true,
 created_at timestamptz NOT NULL, created_by varchar(255) NOT NULL, updated_at timestamptz NOT NULL, updated_by varchar(255) NOT NULL,
 version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL DEFAULT 'ACTIVE', CONSTRAINT uk_eip_gateway_route_code UNIQUE(tenant_id,route_code));
CREATE INDEX ix_eip_gateway_path ON eip_gateway_routes(tenant_id,path_pattern,http_method,active);
CREATE TABLE eip_webhook_subscriptions (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, subscription_code varchar(100) NOT NULL, event_pattern varchar(180) NOT NULL,
 callback_url varchar(600) NOT NULL, secret_ciphertext text NOT NULL, secret_key_id varchar(100) NOT NULL, active boolean NOT NULL DEFAULT true,
 last_delivery_at timestamptz, created_at timestamptz NOT NULL, created_by varchar(255) NOT NULL, updated_at timestamptz NOT NULL,
 updated_by varchar(255) NOT NULL, version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL DEFAULT 'ACTIVE',
 CONSTRAINT uk_eip_webhook_code UNIQUE(tenant_id,subscription_code));
CREATE INDEX ix_eip_webhook_event ON eip_webhook_subscriptions(tenant_id,event_pattern,active);
CREATE TABLE eip_transformation_rules (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, rule_code varchar(100) NOT NULL, source_content_type varchar(100) NOT NULL,
 target_content_type varchar(100) NOT NULL, mapping_expression text NOT NULL, active boolean NOT NULL DEFAULT true,
 created_at timestamptz NOT NULL, created_by varchar(255) NOT NULL, updated_at timestamptz NOT NULL, updated_by varchar(255) NOT NULL,
 version bigint NOT NULL DEFAULT 0, status varchar(30) NOT NULL DEFAULT 'ACTIVE', CONSTRAINT uk_eip_transform_code UNIQUE(tenant_id,rule_code));
CREATE TABLE eip_external_connectors (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, connector_code varchar(100) NOT NULL, connector_type varchar(80) NOT NULL,
 base_url varchar(600) NOT NULL, auth_type varchar(50) NOT NULL, credential_ciphertext text, credential_key_id varchar(100),
 active boolean NOT NULL DEFAULT true, created_at timestamptz NOT NULL, created_by varchar(255) NOT NULL,
 updated_at timestamptz NOT NULL, updated_by varchar(255) NOT NULL, version bigint NOT NULL DEFAULT 0,
 status varchar(30) NOT NULL DEFAULT 'ACTIVE', CONSTRAINT uk_eip_connector_code UNIQUE(tenant_id,connector_code));
CREATE INDEX ix_eip_connector_type ON eip_external_connectors(tenant_id,connector_type,active);
