ALTER TABLE gts_education_level
    ADD CONSTRAINT uq_gts_education_level_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE gts_class_grade
    ADD CONSTRAINT fk_gts_class_grade_education_level_tenant
        FOREIGN KEY (tenant_id, education_level_id)
        REFERENCES gts_education_level (tenant_id, id);
