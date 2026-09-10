ALTER TABLE gts_academic_result_audit_event
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE gts_academic_result_audit_event
ADD CONSTRAINT ck_gts_academic_result_audit_event_status
CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'));
