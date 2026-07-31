ALTER TABLE parent_notification_rules
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE parent_notification_rules
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE parent_notification_rules
ALTER COLUMN status SET NOT NULL;
