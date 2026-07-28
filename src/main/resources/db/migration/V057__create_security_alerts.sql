CREATE TABLE security_alerts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,

    security_finding_id UUID NOT NULL,

    severity VARCHAR(20) NOT NULL,
    alert_status VARCHAR(20) NOT NULL,

    message VARCHAR(500) NOT NULL,

    alert_created_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX ix_security_alerts_tenant
    ON security_alerts(
        tenant_id,
        alert_created_at
    );

CREATE INDEX ix_security_alerts_status
    ON security_alerts(
        tenant_id,
        alert_status
    );
