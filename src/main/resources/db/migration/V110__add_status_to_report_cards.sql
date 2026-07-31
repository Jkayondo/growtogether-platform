ALTER TABLE report_cards
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE report_cards
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE report_cards
ALTER COLUMN status SET NOT NULL;
