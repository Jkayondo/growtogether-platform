CREATE TABLE IF NOT EXISTS visitor_check_ins (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    visitor_request_id UUID NOT NULL,

    visitor_id UUID NOT NULL,

    gate_location VARCHAR(100),

    badge_number VARCHAR(100),

    checked_in_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    checked_out_at TIMESTAMP,

    checked_in_by VARCHAR(150) NOT NULL,

    checked_out_by VARCHAR(150),

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    check_in_status VARCHAR(50) NOT NULL DEFAULT 'CHECKED_IN',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(150) NOT NULL,

    updated_by VARCHAR(150),

    version BIGINT NOT NULL DEFAULT 0
);


CREATE INDEX IF NOT EXISTS ix_visitor_check_ins_tenant
ON visitor_check_ins(tenant_id);


CREATE INDEX IF NOT EXISTS ix_visitor_check_ins_request
ON visitor_check_ins(tenant_id, visitor_request_id);


CREATE INDEX IF NOT EXISTS ix_visitor_check_ins_visitor
ON visitor_check_ins(tenant_id, visitor_id);


CREATE INDEX IF NOT EXISTS ix_visitor_check_ins_check_status
ON visitor_check_ins(tenant_id, check_in_status);
