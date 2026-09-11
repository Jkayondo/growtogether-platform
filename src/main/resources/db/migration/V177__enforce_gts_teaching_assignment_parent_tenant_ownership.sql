-- GT-SCH-TEACHING-ASSIGNMENT-DB-INTEGRITY-001
--
-- Additive tenant-isolation hardening for Teaching Assignment.
--
-- Existing single-column foreign keys are intentionally retained.
-- Existing data was audited before this migration:
--   * 0 teaching assignments existed;
--   * 0 cross-tenant parent references were found.
--
-- Composite tenant-aware foreign keys ensure that every parent
-- referenced by a Teaching Assignment belongs to the same tenant.


-- ============================================================
-- Parent candidate keys not already provided by V175/V176
-- ============================================================

ALTER TABLE gts_teacher_profile
    ADD CONSTRAINT uq_gts_teacher_profile_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE ewf_assignment
    ADD CONSTRAINT uq_ewf_assignment_tenant_id
    UNIQUE (tenant_id, id);


-- ============================================================
-- Required Teaching Assignment parents
-- ============================================================

ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_teacher_profile_tenant
    FOREIGN KEY (tenant_id, teacher_profile_id)
    REFERENCES gts_teacher_profile (tenant_id, id);


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_academic_year_tenant
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_campus_tenant
    FOREIGN KEY (tenant_id, campus_id)
    REFERENCES gts_campus (tenant_id, id);


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_class_grade_tenant
    FOREIGN KEY (tenant_id, class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_subject_tenant
    FOREIGN KEY (tenant_id, subject_id)
    REFERENCES gts_subject (tenant_id, id);


-- ============================================================
-- Optional Teaching Assignment parents
-- ============================================================

ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_academic_term_tenant
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_stream_tenant
    FOREIGN KEY (tenant_id, stream_id)
    REFERENCES gts_stream (tenant_id, id);


ALTER TABLE gts_teaching_assignment
    ADD CONSTRAINT fk_gts_teaching_assignment_ewf_assignment_tenant
    FOREIGN KEY (tenant_id, ewf_assignment_id)
    REFERENCES ewf_assignment (tenant_id, id);
