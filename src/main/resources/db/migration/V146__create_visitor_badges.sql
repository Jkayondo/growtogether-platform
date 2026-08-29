CREATE TABLE IF NOT EXISTS visitor_badges (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    visitor_check_in_id UUID NOT NULL,

    badge_number VARCHAR(100) NOT NULL,

    badge_type VARCHAR(50),

    badge_status VARCHAR(50) NOT NULL DEFAULT 'ISSUED',

    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    issued_by VARCHAR(150) NOT NULL,

    returned_at TIMESTAMPTZ,

    returned_by VARCHAR(150),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT ck_visitor_badges_badge_status
        CHECK (badge_status IN ('ISSUED', 'RETURNED', 'DISABLED')),

    CONSTRAINT ck_visitor_badges_entity_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);


CREATE INDEX IF NOT EXISTS ix_visitor_badges_tenant
    ON visitor_badges(tenant_id);


CREATE INDEX IF NOT EXISTS ix_visitor_badges_check_in
    ON visitor_badges(tenant_id, visitor_check_in_id);


CREATE INDEX IF NOT EXISTS ix_visitor_badges_number
    ON visitor_badges(tenant_id, badge_number);


CREATE INDEX IF NOT EXISTS ix_visitor_badges_status
    ON visitor_badges(tenant_id, badge_status);
