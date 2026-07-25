CREATE TABLE eaif_execution_audits (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,

    ai_request_id UUID NOT NULL,

    source_service VARCHAR(100) NOT NULL,
    model_code VARCHAR(100) NOT NULL,
    prompt_code VARCHAR(100),

    risk_level VARCHAR(20) NOT NULL,
    execution_status VARCHAR(30) NOT NULL,

    actor_user_id UUID,

    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    output_reference VARCHAR(500),

    created_at TIMESTAMP NOT NULL,
    created_by UUID,

    updated_at TIMESTAMP NOT NULL,
    updated_by UUID,

    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_eaif_audit_request
    ON eaif_execution_audits(
        tenant_id,
        ai_request_id
    );

CREATE INDEX ix_eaif_audit_status
    ON eaif_execution_audits(
        tenant_id,
        execution_status
    );

CREATE INDEX ix_eaif_audit_created
    ON eaif_execution_audits(
        tenant_id,
        created_at
    );
