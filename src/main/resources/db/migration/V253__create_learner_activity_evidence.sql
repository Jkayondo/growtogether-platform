-- GT Learner Activity Evidence Foundation

CREATE TABLE IF NOT EXISTS learner_activity_evidence (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    student_id UUID NOT NULL,

    activity_id UUID NOT NULL,

    activity_type VARCHAR(100),

    completion_status VARCHAR(50),

    performance_score NUMERIC(10,2),

    completed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_learner_activity_evidence_tenant
ON learner_activity_evidence(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_learner_activity_evidence_student
ON learner_activity_evidence(
    tenant_id,
    student_id
);


CREATE INDEX IF NOT EXISTS ix_learner_activity_evidence_activity
ON learner_activity_evidence(
    tenant_id,
    activity_id
);

