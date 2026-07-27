CREATE TABLE eaif_approval_records (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    ai_request_id UUID NOT NULL,

    approval_status VARCHAR(30) NOT NULL,

    requested_at TIMESTAMP NOT NULL,

    approved_by UUID,

    approved_at TIMESTAMP,

    decision_reason VARCHAR(500),

    created_at TIMESTAMP NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);


CREATE INDEX ix_eaif_approval_request
    ON eaif_approval_records(
        tenant_id,
        ai_request_id
    );


CREATE INDEX ix_eaif_approval_status
    ON eaif_approval_records(
        tenant_id,
        approval_status
    );
