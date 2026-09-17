CREATE TABLE learner_performance_explanations (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    student_id UUID NOT NULL,

    subject_name VARCHAR(200),

    explanation VARCHAR(3000),

    evidence_summary VARCHAR(3000),

    confidence_level VARCHAR(50),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL

);
