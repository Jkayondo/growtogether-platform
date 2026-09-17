-- GT AI Review Record Audit Foundation

CREATE TABLE IF NOT EXISTS ai_review_records (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    audit_id UUID NOT NULL,

    reviewer_role VARCHAR(200),

    review_comment VARCHAR(5000),

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_ai_review_records_tenant
ON ai_review_records(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_ai_review_records_audit
ON ai_review_records(
    tenant_id,
    audit_id
);

