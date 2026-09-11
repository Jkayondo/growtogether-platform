-- GT-SCH-CLASS-OFFERING-DB-INTEGRITY-001
--
-- Ensures every Class Offering parent reference belongs to
-- the same tenant as the Class Offering itself.
--
-- Existing single-column foreign keys are intentionally retained.
-- These composite constraints provide tenant-isolation
-- defence in depth.


-- =========================================================
-- Composite parent candidate keys
-- =========================================================

ALTER TABLE gts_academic_year
    ADD CONSTRAINT uq_gts_academic_year_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_campus
    ADD CONSTRAINT uq_gts_campus_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_class_grade
    ADD CONSTRAINT uq_gts_class_grade_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_academic_programme
    ADD CONSTRAINT uq_gts_academic_programme_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_study_track
    ADD CONSTRAINT uq_gts_study_track_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_curriculum_version
    ADD CONSTRAINT uq_gts_curriculum_version_tenant_id
        UNIQUE (tenant_id, id);


-- =========================================================
-- Required Class Offering parents
-- =========================================================

ALTER TABLE gts_class_offering
    ADD CONSTRAINT fk_gts_class_offering_academic_year_tenant
        FOREIGN KEY (
            tenant_id,
            academic_year_id
        )
        REFERENCES gts_academic_year (
            tenant_id,
            id
        );


ALTER TABLE gts_class_offering
    ADD CONSTRAINT fk_gts_class_offering_campus_tenant
        FOREIGN KEY (
            tenant_id,
            campus_id
        )
        REFERENCES gts_campus (
            tenant_id,
            id
        );


ALTER TABLE gts_class_offering
    ADD CONSTRAINT fk_gts_class_offering_class_grade_tenant
        FOREIGN KEY (
            tenant_id,
            class_grade_id
        )
        REFERENCES gts_class_grade (
            tenant_id,
            id
        );


-- =========================================================
-- Optional Class Offering parents
-- =========================================================

ALTER TABLE gts_class_offering
    ADD CONSTRAINT fk_gts_class_offering_academic_programme_tenant
        FOREIGN KEY (
            tenant_id,
            academic_programme_id
        )
        REFERENCES gts_academic_programme (
            tenant_id,
            id
        );


ALTER TABLE gts_class_offering
    ADD CONSTRAINT fk_gts_class_offering_study_track_tenant
        FOREIGN KEY (
            tenant_id,
            study_track_id
        )
        REFERENCES gts_study_track (
            tenant_id,
            id
        );


ALTER TABLE gts_class_offering
    ADD CONSTRAINT fk_gts_class_offering_curriculum_version_tenant
        FOREIGN KEY (
            tenant_id,
            curriculum_version_id
        )
        REFERENCES gts_curriculum_version (
            tenant_id,
            id
        );
