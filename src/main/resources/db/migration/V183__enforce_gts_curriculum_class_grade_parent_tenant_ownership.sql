-- GT-SCH-CURRICULUM-CLASS-GRADE-DB-INTEGRITY-001
--
-- Ensures every required Curriculum Class Grade parent
-- belongs to the same tenant as the mapping itself.
--
-- Parent composite candidate keys already exist from V175.
-- Existing single-column foreign keys are intentionally retained.
-- These composite constraints provide tenant-isolation
-- defence in depth.


-- =========================================================
-- Curriculum Version parent
-- =========================================================

ALTER TABLE gts_curriculum_class_grade
    ADD CONSTRAINT fk_gts_curriculum_class_grade_version_tenant
        FOREIGN KEY (
            tenant_id,
            curriculum_version_id
        )
        REFERENCES gts_curriculum_version (
            tenant_id,
            id
        )
        ON DELETE CASCADE;


-- =========================================================
-- Class Grade parent
-- =========================================================

ALTER TABLE gts_curriculum_class_grade
    ADD CONSTRAINT fk_gts_curriculum_class_grade_class_grade_tenant
        FOREIGN KEY (
            tenant_id,
            class_grade_id
        )
        REFERENCES gts_class_grade (
            tenant_id,
            id
        );
