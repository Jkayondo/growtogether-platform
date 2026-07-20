CREATE TABLE eip_messages (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, event_type varchar(160) NOT NULL, event_version varchar(30) NOT NULL,
 source_service varchar(100) NOT NULL, destination varchar(200) NOT NULL, protocol varchar(30) NOT NULL,
 payload text NOT NULL, headers_json text, correlation_id varchar(100), idempotency_key varchar(180) NOT NULL,
 message_status varchar(30) NOT NULL, attempt_count integer NOT NULL DEFAULT 0, max_attempts integer NOT NULL,
 next_attempt_at timestamptz, delivered_at timestamptz, last_error text, replay_of_message_id uuid,
 created_at timestamptz NOT NULL, created_by varchar(150) NOT NULL, updated_at timestamptz NOT NULL,
 updated_by varchar(150) NOT NULL, version bigint NOT NULL DEFAULT 0, status varchar(20) NOT NULL,
 CONSTRAINT uk_eip_message_tenant_idempotency UNIQUE (tenant_id,idempotency_key)
);
CREATE INDEX ix_eip_message_tenant_status ON eip_messages(tenant_id,message_status,next_attempt_at);
CREATE INDEX ix_eip_message_event ON eip_messages(tenant_id,event_type,created_at);
CREATE TABLE eip_routes (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, route_code varchar(100) NOT NULL, event_pattern varchar(180) NOT NULL,
 destination varchar(200) NOT NULL, protocol varchar(30) NOT NULL, priority integer NOT NULL, enabled boolean NOT NULL DEFAULT true,
 created_at timestamptz NOT NULL, created_by varchar(150) NOT NULL, updated_at timestamptz NOT NULL,
 updated_by varchar(150) NOT NULL, version bigint NOT NULL DEFAULT 0, status varchar(20) NOT NULL,
 CONSTRAINT uk_eip_route_tenant_code UNIQUE(tenant_id,route_code)
);
CREATE TABLE eip_circuits (
 id uuid PRIMARY KEY, tenant_id uuid NOT NULL, destination varchar(200) NOT NULL, circuit_state varchar(20) NOT NULL,
 failure_count integer NOT NULL DEFAULT 0, opened_at timestamptz, next_probe_at timestamptz,
 created_at timestamptz NOT NULL, created_by varchar(150) NOT NULL, updated_at timestamptz NOT NULL,
 updated_by varchar(150) NOT NULL, version bigint NOT NULL DEFAULT 0, status varchar(20) NOT NULL,
 CONSTRAINT uk_eip_circuit_tenant_destination UNIQUE(tenant_id,destination)
);
