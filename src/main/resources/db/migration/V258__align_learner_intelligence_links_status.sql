-- GT Learner Intelligence Link Entity Alignment
-- Adds missing status required by entity schema validation

ALTER TABLE learner_intelligence_links
    ADD COLUMN IF NOT EXISTS status VARCHAR(100);

