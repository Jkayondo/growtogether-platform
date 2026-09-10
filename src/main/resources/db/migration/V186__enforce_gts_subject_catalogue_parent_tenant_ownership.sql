-- GT-SCH-SUBJECT-CATALOGUE-DB-INTEGRITY-001
--
-- Enforces Subject Catalogue tenant and exact-parent ownership.
--
-- A catalogue entry must reference:
--   1. a valid tenant;
--   2. a Curriculum Version belonging to that tenant;
--   3. a Learning Area belonging to that exact tenant
--      and exact Curriculum Version.
--
-- Existing single-column parent FKs are intentionally retained.


-- =========================================================
-- Parent candidate key required by exact Learning Area FK
-- =========================================================

ALTER TABLE gts_curriculum_learning_area
    ADD CONSTRAINT uq_gts_curriculum_learning_area_tenant_version_id
        UNIQUE (
            tenant_id,
            curriculum_version_id,
            id
        );


-- =========================================================
-- Direct tenant ownership
-- =========================================================

ALTER TABLE gts_subject_catalogue
    ADD CONSTRAINT fk_gts_subject_catalogue_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES eiam_tenant (id);


-- =========================================================
-- Curriculum Version tenant ownership
-- =========================================================

ALTER TABLE gts_subject_catalogue
    ADD CONSTRAINT fk_gts_subject_catalogue_version_tenant
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
-- Exact Learning Area parent ownership
-- =========================================================

ALTER TABLE gts_subject_catalogue
    ADD CONSTRAINT fk_gts_subject_catalogue_learning_area_exact_parent
        FOREIGN KEY (
            tenant_id,
            curriculum_version_id,
            learning_area_id
        )
        REFERENCES gts_curriculum_learning_area (
            tenant_id,
            curriculum_version_id,
            id
        )
        ON DELETE CASCADE;
