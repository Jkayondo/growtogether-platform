CREATE TABLE security_incidents (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    security_alert_id UUID NOT NULL
        REFERENCES security_alerts(id),

    incident_number VARCHAR(50) NOT NULL,

    title VARCHAR(200) NOT NULL,

    description TEXT,

    severity VARCHAR(20) NOT NULL,

    incident_status VARCHAR(30) NOT NULL,

    assigned_to UUID,

    opened_at TIMESTAMPTZ NOT NULL,

    resolved_at TIMESTAMPTZ,


    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,


    CONSTRAINT uq_security_incident_number
        UNIQUE (tenant_id, incident_number),


    CONSTRAINT ck_security_incident_severity
        CHECK (
            severity IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'CRITICAL'
            )
        ),


    CONSTRAINT ck_security_incident_status
        CHECK (
            incident_status IN (
                'OPEN',
                'INVESTIGATING',
                'RESOLVED',
                'CLOSED'
            )
        ),


    CONSTRAINT ck_security_incident_lifecycle
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);


CREATE INDEX ix_security_incident_tenant
    ON security_incidents(tenant_id);


CREATE INDEX ix_security_incident_status
    ON security_incidents(
        tenant_id,
        status
    );
