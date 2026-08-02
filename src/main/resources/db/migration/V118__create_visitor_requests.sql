CREATE TABLE IF NOT EXISTS visitor_requests (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    visitor_id UUID NOT NULL,

    host_user_id UUID,

    purpose VARCHAR(255) NOT NULL,

    visit_date DATE NOT NULL,

    expected_arrival_time TIME,

    expected_departure_time TIME,

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    request_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    approval_comment VARCHAR(500),

    approved_by VARCHAR(150),

    approved_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(150) NOT NULL,

    updated_by VARCHAR(150),

    version BIGINT NOT NULL DEFAULT 0
);


CREATE INDEX IF NOT EXISTS ix_visitor_requests_tenant
ON visitor_requests(tenant_id);


CREATE INDEX IF NOT EXISTS ix_visitor_requests_visitor
ON visitor_requests(tenant_id, visitor_id);


CREATE INDEX IF NOT EXISTS ix_visitor_requests_request_status
ON visitor_requests(tenant_id, request_status);
