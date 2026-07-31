CREATE TABLE IF NOT EXISTS report_cards (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    learner_id UUID NOT NULL,

    academic_period_id UUID NOT NULL,

    overall_comment VARCHAR(500),

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255),

    version BIGINT

);

CREATE INDEX IF NOT EXISTS ix_report_card_tenant
ON report_cards (tenant_id);
