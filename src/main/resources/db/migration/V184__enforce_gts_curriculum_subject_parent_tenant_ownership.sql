-- GT-SCH-CURRICULUM-SUBJECT-DB-INTEGRITY-001
--
-- Ensures every Curriculum Subject parent reference belongs
-- to the same tenant as the Curriculum Subject mapping.
--
-- Composite parent candidate keys already exist from V175/V176.
-- Existing single-column foreign keys are intentionally retained.
-- These composite constraints provide tenant-isolation
-- defence in depth.


-- =========================================================
-- Required parents
-- =========================================================

ALTER TABLE gts_curriculum_subject
    ADD CONSTRAINT fk_gts_curriculum_subject_curriculum_version_tenant
        FOREIGN KEY (
            tenant_id,
            curriculum_version_id
        )
        REFERENCES gts_curriculum_version (
            tenant_id,
            id
        )
        ON DELETE CASCADE;


ALTER TABLE gts_curriculum_subject
    ADD CONSTRAINT fk_gts_curriculum_subject_class_grade_tenant
        FOREIGN KEY (
            tenant_id,
            class_grade_id
        )
        REFERENCES gts_class_grade (
            tenant_id,
            id
        );


ALTER TABLE gts_curriculum_subject
    ADD CONSTRAINT fk_gts_curriculum_subject_subject_tenant
        FOREIGN KEY (
            tenant_id,
            subject_id
        )
        REFERENCES gts_subject (
            tenant_id,
            id
        );


-- =========================================================
-- Optional parents
-- =========================================================

ALTER TABLE gts_curriculum_subject
    ADD CONSTRAINT fk_gts_curriculum_subject_academic_programme_tenant
        FOREIGN KEY (
            tenant_id,
            academic_programme_id
        )
        REFERENCES gts_academic_programme (
            tenant_id,
            id
        );


ALTER TABLE gts_curriculum_subject
    ADD CONSTRAINT fk_gts_curriculum_subject_study_track_tenant
        FOREIGN KEY (
            tenant_id,
            study_track_id
        )
        REFERENCES gts_study_track (
            tenant_id,
            id
        );
