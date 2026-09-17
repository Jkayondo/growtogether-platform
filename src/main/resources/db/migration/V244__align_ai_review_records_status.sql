-- GT AI Review Record Entity Alignment
-- Adds status required by AuditedTenantEntity

ALTER TABLE ai_review_records
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

