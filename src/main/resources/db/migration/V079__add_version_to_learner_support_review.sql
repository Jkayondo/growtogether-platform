ALTER TABLE gts_learner_support_review
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
