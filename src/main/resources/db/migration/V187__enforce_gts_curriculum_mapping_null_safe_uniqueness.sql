-- GT-SCH-CURRICULUM-MAPPING-UNIQUENESS-001
--
-- PostgreSQL UNIQUE constraints normally treat NULL values as distinct.
-- academic_programme_id and study_track_id are optional on the curriculum
-- mapping tables, therefore the original uniqueness constraints could allow
-- duplicate default-scope mappings.
--
-- PostgreSQL 15+ supports UNIQUE NULLS NOT DISTINCT, which gives the intended
-- business semantics without introducing sentinel values.

-- ============================================================
-- Curriculum Version -> Class Grade mapping
-- ============================================================

ALTER TABLE gts_curriculum_class_grade
    DROP CONSTRAINT uq_gts_curriculum_class_grade;

ALTER TABLE gts_curriculum_class_grade
    ADD CONSTRAINT uq_gts_curriculum_class_grade
    UNIQUE NULLS NOT DISTINCT (
        tenant_id,
        curriculum_version_id,
        academic_programme_id,
        study_track_id,
        class_grade_id
    );


ALTER TABLE gts_curriculum_class_grade
    DROP CONSTRAINT uq_gts_curriculum_class_sequence;

ALTER TABLE gts_curriculum_class_grade
    ADD CONSTRAINT uq_gts_curriculum_class_sequence
    UNIQUE NULLS NOT DISTINCT (
        tenant_id,
        curriculum_version_id,
        academic_programme_id,
        study_track_id,
        sequence_number
    );


-- ============================================================
-- Curriculum Version + Grade -> Subject mapping
-- ============================================================

ALTER TABLE gts_curriculum_subject
    DROP CONSTRAINT uq_gts_curriculum_subject;

ALTER TABLE gts_curriculum_subject
    ADD CONSTRAINT uq_gts_curriculum_subject
    UNIQUE NULLS NOT DISTINCT (
        tenant_id,
        curriculum_version_id,
        academic_programme_id,
        study_track_id,
        class_grade_id,
        subject_id
    );
