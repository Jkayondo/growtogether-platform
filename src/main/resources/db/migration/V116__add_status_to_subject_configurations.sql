ALTER TABLE subject_configurations
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE subject_configurations
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE subject_configurations
ALTER COLUMN status SET NOT NULL;
