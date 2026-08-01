CREATE TABLE IF NOT EXISTS school_visitors (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    phone_number VARCHAR(50),

    email VARCHAR(150),

    identification_type VARCHAR(50),

    identification_reference VARCHAR(100),

    visitor_category VARCHAR(50),

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(150) NOT NULL,

    updated_by VARCHAR(150),

    version BIGINT NOT NULL DEFAULT 0
);


CREATE INDEX IF NOT EXISTS ix_school_visitors_tenant
ON school_visitors(tenant_id);


CREATE INDEX IF NOT EXISTS ix_school_visitors_identity
ON school_visitors(tenant_id, identification_reference);
