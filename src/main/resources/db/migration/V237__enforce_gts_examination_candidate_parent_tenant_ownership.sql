-- GT School
-- Enforce tenant ownership across Examination Candidate parent relationships.
--
-- Application validation already enforces these boundaries.
-- This migration adds equivalent database-level tenant protection.


-- Examination Session requires a tenant-qualified candidate key
-- for composite foreign-key references.

ALTER TABLE gts_examination_session
    ADD CONSTRAINT uq_gts_examination_session_tenant_id
    UNIQUE (tenant_id, id);


-- Examination Session must belong to the same tenant as the Candidate.
-- Preserve the existing session -> candidate cascade semantics.

ALTER TABLE gts_examination_candidate
    ADD CONSTRAINT fk_gts_examination_candidate_session_tenant
    FOREIGN KEY (tenant_id, examination_session_id)
    REFERENCES gts_examination_session (tenant_id, id)
    ON DELETE CASCADE;


-- Student must belong to the same tenant as the Candidate.
-- gts_student (tenant_id, id) is established by V180.

ALTER TABLE gts_examination_candidate
    ADD CONSTRAINT fk_gts_examination_candidate_student_tenant
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);


-- Student Enrollment must belong to the same tenant as the Candidate.
-- gts_student_enrollment (tenant_id, id) is established by V180.

ALTER TABLE gts_examination_candidate
    ADD CONSTRAINT fk_gts_examination_candidate_enrollment_tenant
    FOREIGN KEY (tenant_id, student_enrollment_id)
    REFERENCES gts_student_enrollment (tenant_id, id);
