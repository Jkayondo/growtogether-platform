CREATE TABLE ens_notification_template_versions (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL,

    template_id UUID NOT NULL,

    version_number INTEGER NOT NULL,

    language_code VARCHAR(10) NOT NULL,

    subject_template VARCHAR(300),

    body_template TEXT NOT NULL,

    variables JSONB NOT NULL DEFAULT '{}',

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    change_reason VARCHAR(500),

    created_by UUID,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    checksum VARCHAR(64),

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT fk_ens_template_versions_template

        FOREIGN KEY (template_id)

        REFERENCES ens_notification_templates(id),


    CONSTRAINT uk_ens_template_version_language

        UNIQUE
        (
            template_id,
            version_number,
            language_code
        )
);


CREATE INDEX ix_ens_template_versions_template
ON ens_notification_template_versions
(
    template_id,
    version_number
);


CREATE INDEX ix_ens_template_versions_tenant
ON ens_notification_template_versions
(
    tenant_id
);
