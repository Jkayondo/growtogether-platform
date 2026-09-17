-- Align academic_risk_indicators table with AcademicRiskIndicator entity

ALTER TABLE academic_risk_indicators
    ADD COLUMN IF NOT EXISTS student_id UUID;

ALTER TABLE academic_risk_indicators
    ADD COLUMN IF NOT EXISTS subject_id UUID;

ALTER TABLE academic_risk_indicators
    ADD COLUMN IF NOT EXISTS subject_name VARCHAR(200);

ALTER TABLE academic_risk_indicators
    ADD COLUMN IF NOT EXISTS risk_type VARCHAR(100);

ALTER TABLE academic_risk_indicators
    ADD COLUMN IF NOT EXISTS recommended_action VARCHAR(2000);

ALTER TABLE academic_risk_indicators
    ADD COLUMN IF NOT EXISTS status VARCHAR(50)
        NOT NULL DEFAULT 'ACTIVE';


ALTER TABLE academic_risk_indicators
    ALTER COLUMN student_id SET NOT NULL;

ALTER TABLE academic_risk_indicators
    ALTER COLUMN risk_type SET NOT NULL;


CREATE INDEX IF NOT EXISTS ix_academic_risk_indicator_student
ON academic_risk_indicators(
    tenant_id,
    student_id
);


CREATE INDEX IF NOT EXISTS ix_academic_risk_indicator_subject
ON academic_risk_indicators(
    tenant_id,
    subject_id
);
