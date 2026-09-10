-- GT-ACADEMIC-RESULT-TRACEABILITY-001
--
-- Enhances academic result records with calculation evidence references.
--
-- No grading logic is stored here.
-- This migration stores traceability links only.


ALTER TABLE academic_result_record

ADD COLUMN IF NOT EXISTS class_id UUID,


ADD COLUMN IF NOT EXISTS assessment_id UUID,


ADD COLUMN IF NOT EXISTS aggregation_rule_id UUID,


ADD COLUMN IF NOT EXISTS division_rule_id UUID;
