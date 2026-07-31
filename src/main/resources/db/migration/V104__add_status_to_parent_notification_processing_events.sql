ALTER TABLE parent_notification_processing_events
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE parent_notification_processing_events
SET status = 'RECEIVED'
WHERE status IS NULL;

ALTER TABLE parent_notification_processing_events
ALTER COLUMN status SET NOT NULL;
