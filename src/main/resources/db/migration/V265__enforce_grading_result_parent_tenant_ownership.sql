-- Harden established Grading / Results parent relationships so that
-- child and parent rows must belong to the same tenant.
--
-- Existing ID-only foreign keys are deliberately preserved.
-- This migration adds tenant-aware ownership constraints only for
-- parent relationships already enforced by the database.

-- ------------------------------------------------------------------
-- Grading configuration -> Grading Scheme
-- ------------------------------------------------------------------

ALTER TABLE gts_grade_boundary
    ADD CONSTRAINT fk_gts_grade_boundary_grading_scheme_tenant
    FOREIGN KEY (tenant_id, grading_scheme_id)
    REFERENCES gts_grading_scheme (tenant_id, id)
    ON DELETE CASCADE;

ALTER TABLE gts_grade_aggregation_rule
    ADD CONSTRAINT fk_gts_grade_aggregation_rule_grading_scheme_tenant
    FOREIGN KEY (tenant_id, grading_scheme_id)
    REFERENCES gts_grading_scheme (tenant_id, id)
    ON DELETE CASCADE;

ALTER TABLE gts_grade_division_rule
    ADD CONSTRAINT fk_gts_grade_division_rule_grading_scheme_tenant
    FOREIGN KEY (tenant_id, grading_scheme_id)
    REFERENCES gts_grading_scheme (tenant_id, id)
    ON DELETE CASCADE;

-- ------------------------------------------------------------------
-- Academic Result Record -> Student
-- ------------------------------------------------------------------

ALTER TABLE academic_result_record
    ADD CONSTRAINT fk_academic_result_record_student_tenant
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);

-- ------------------------------------------------------------------
-- Result Publication -> Academic structure
-- ------------------------------------------------------------------

ALTER TABLE gts_result_publication
    ADD CONSTRAINT fk_gts_result_publication_academic_year_tenant
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);

ALTER TABLE gts_result_publication
    ADD CONSTRAINT fk_gts_result_publication_academic_term_tenant
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);

ALTER TABLE gts_result_publication
    ADD CONSTRAINT fk_gts_result_publication_class_grade_tenant
    FOREIGN KEY (tenant_id, class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);
