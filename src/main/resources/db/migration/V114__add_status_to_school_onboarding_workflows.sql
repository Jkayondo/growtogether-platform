ALTER TABLE school_onboarding_workflows
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE school_onboarding_workflows
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE school_onboarding_workflows
ALTER COLUMN status SET NOT NULL;
