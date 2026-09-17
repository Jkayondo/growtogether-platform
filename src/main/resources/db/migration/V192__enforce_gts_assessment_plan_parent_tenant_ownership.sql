-- GT-SCH-ASSESSMENT-PLAN-PARENT-TENANT-INTEGRITY-001

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_academic_year_tenant
        FOREIGN KEY (tenant_id, academic_year_id)
        REFERENCES gts_academic_year (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_academic_term_tenant
        FOREIGN KEY (tenant_id, academic_term_id)
        REFERENCES gts_academic_term (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_campus_tenant
        FOREIGN KEY (tenant_id, campus_id)
        REFERENCES gts_campus (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_academic_programme_tenant
        FOREIGN KEY (tenant_id, academic_programme_id)
        REFERENCES gts_academic_programme (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_study_track_tenant
        FOREIGN KEY (tenant_id, study_track_id)
        REFERENCES gts_study_track (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_curriculum_version_tenant
        FOREIGN KEY (tenant_id, curriculum_version_id)
        REFERENCES gts_curriculum_version (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_class_grade_tenant
        FOREIGN KEY (tenant_id, class_grade_id)
        REFERENCES gts_class_grade (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_stream_tenant
        FOREIGN KEY (tenant_id, stream_id)
        REFERENCES gts_stream (tenant_id, id);

ALTER TABLE gts_assessment_plan
    ADD CONSTRAINT fk_gts_assessment_plan_grading_scheme_tenant
        FOREIGN KEY (tenant_id, grading_scheme_id)
        REFERENCES gts_grading_scheme (tenant_id, id);
