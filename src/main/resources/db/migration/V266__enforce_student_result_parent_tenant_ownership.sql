-- Harden established GT School student-result parent relationships so that
-- result records cannot reference parent rows belonging to another tenant.
--
-- Existing ID-only foreign keys are deliberately preserved.
--
-- Three parent tables require (tenant_id, id) candidate keys before their
-- existing relationships can be hardened. Their globally unique primary-key
-- IDs already guarantee uniqueness of these composite pairs.

-- -------------------------------------------------------------------------
-- Parent candidate keys
-- -------------------------------------------------------------------------

ALTER TABLE gts_grade_calculation_run
    ADD CONSTRAINT uq_gts_grade_calculation_run_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_student_grade_outcome
    ADD CONSTRAINT uq_gts_student_grade_outcome_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT uq_gts_assessment_plan_tenant_id
    UNIQUE (tenant_id, id);


-- -------------------------------------------------------------------------
-- Student Subject Result parent ownership
-- -------------------------------------------------------------------------

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_student_tenant
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_student_enrollment_tenant
    FOREIGN KEY (tenant_id, student_enrollment_id)
    REFERENCES gts_student_enrollment (tenant_id, id);

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_academic_year_tenant
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_academic_term_tenant
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_subject_offering_tenant
    FOREIGN KEY (tenant_id, subject_offering_id)
    REFERENCES gts_subject_offering (tenant_id, id);

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_grade_calculation_run_tenant
    FOREIGN KEY (tenant_id, grade_calculation_run_id)
    REFERENCES gts_grade_calculation_run (tenant_id, id);

ALTER TABLE gts_student_subject_result
    ADD CONSTRAINT fk_gts_student_subject_result_grade_outcome_tenant
    FOREIGN KEY (tenant_id, grade_outcome_id)
    REFERENCES gts_student_grade_outcome (tenant_id, id);


-- -------------------------------------------------------------------------
-- Student Term Result parent ownership
-- -------------------------------------------------------------------------

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_student_tenant
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_student_enrollment_tenant
    FOREIGN KEY (tenant_id, student_enrollment_id)
    REFERENCES gts_student_enrollment (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_academic_year_tenant
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_academic_term_tenant
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_class_grade_tenant
    FOREIGN KEY (tenant_id, class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_stream_tenant
    FOREIGN KEY (tenant_id, stream_id)
    REFERENCES gts_stream (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT fk_gts_student_term_result_assessment_plan_tenant
    FOREIGN KEY (tenant_id, assessment_plan_id)
    REFERENCES gts_assessment_plan (tenant_id, id);
