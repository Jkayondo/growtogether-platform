CREATE TABLE security_findings (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,

    audit_event_id UUID NOT NULL,

    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(20) NOT NULL,

    reason VARCHAR(500) NOT NULL,

    detected_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX ix_security_findings_tenant
    ON security_findings(
        tenant_id,
        detected_at
    );

CREATE INDEX ix_security_findings_risk
    ON security_findings(
        tenant_id,
        risk_level
    );
