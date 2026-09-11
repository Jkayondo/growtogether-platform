-- GT-SCH-CURRICULUM-LEARNING-AREA-DB-INTEGRITY-001
--
-- Adds database-level tenant ownership enforcement for
-- Curriculum Learning Areas.
--
-- Existing single-column Curriculum Version FK is retained.
-- Composite FK provides tenant-isolation defence in depth.


ALTER TABLE gts_curriculum_learning_area
    ADD CONSTRAINT fk_gts_curriculum_learning_area_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES eiam_tenant (id);


ALTER TABLE gts_curriculum_learning_area
    ADD CONSTRAINT fk_gts_curriculum_learning_area_version_tenant
        FOREIGN KEY (
            tenant_id,
            curriculum_version_id
        )
        REFERENCES gts_curriculum_version (
            tenant_id,
            id
        )
        ON DELETE CASCADE;
