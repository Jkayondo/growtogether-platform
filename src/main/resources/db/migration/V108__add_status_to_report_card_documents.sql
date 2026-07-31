ALTER TABLE report_card_documents
ADD COLUMN IF NOT EXISTS status VARCHAR(30);

UPDATE report_card_documents
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE report_card_documents
ALTER COLUMN status SET NOT NULL;
