CREATE TABLE ens_notification_templates (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    template_code VARCHAR(120) NOT NULL,

    name VARCHAR(180) NOT NULL,

    description VARCHAR(1000),

    channel VARCHAR(30) NOT NULL,

    category VARCHAR(80),

    default_language VARCHAR(10) NOT NULL DEFAULT 'en',

    active_version INTEGER,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    created_by UUID,

    approved_by UUID,

    approved_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_ens_template_tenant_code
        UNIQUE (tenant_id, template_code)
);


CREATE INDEX ix_ens_template_tenant_status
ON ens_notification_templates
(
    tenant_id,
    status
);


CREATE INDEX ix_ens_template_tenant_code_lookup
ON ens_notification_templates
(
    tenant_id,
    template_code
);
