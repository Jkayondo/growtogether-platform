ALTER TABLE parent_engagement_scheduled_reports
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE parent_engagement_scheduled_reports
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE parent_engagement_scheduled_reports
ALTER COLUMN status SET NOT NULL;
