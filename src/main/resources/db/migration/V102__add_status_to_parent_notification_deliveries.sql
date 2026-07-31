ALTER TABLE parent_notification_deliveries
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE parent_notification_deliveries
SET status = 'PENDING'
WHERE status IS NULL;

ALTER TABLE parent_notification_deliveries
ALTER COLUMN status SET NOT NULL;
