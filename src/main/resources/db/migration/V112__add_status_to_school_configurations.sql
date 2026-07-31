ALTER TABLE school_configurations
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE school_configurations
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE school_configurations
ALTER COLUMN status SET NOT NULL;
