ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS learner_intelligence_snapshot_id UUID;

ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS risk_level VARCHAR(50);

ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS support_reason TEXT;

ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS support_strategy TEXT;

ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS assigned_staff_id UUID;

ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS review_date DATE;

ALTER TABLE gts_learner_support_plan
ADD COLUMN IF NOT EXISTS support_status VARCHAR(50);
