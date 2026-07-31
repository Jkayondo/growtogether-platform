CREATE TABLE IF NOT EXISTS report_card_documents (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    report_card_id UUID NOT NULL,

    document_status VARCHAR(30) NOT NULL,

    document_reference VARCHAR(100) NOT NULL,

    generated_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_report_card_document_tenant
ON report_card_documents (tenant_id);
