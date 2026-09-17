-- GT Learner Activity Evidence Entity Alignment
-- Adds inherited status column required by AuditedTenantEntity

ALTER TABLE learner_activity_evidence
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

