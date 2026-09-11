-- GT School
-- Enforce tenant ownership for Admission Application parent relationships.

ALTER TABLE gts_admission_application
    ADD CONSTRAINT fk_gts_admission_application_academic_year_tenant
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);

ALTER TABLE gts_admission_application
    ADD CONSTRAINT fk_gts_admission_application_campus_tenant
    FOREIGN KEY (tenant_id, campus_id)
    REFERENCES gts_campus (tenant_id, id);

ALTER TABLE gts_admission_application
    ADD CONSTRAINT fk_gts_admission_application_desired_class_grade_tenant
    FOREIGN KEY (tenant_id, desired_class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);

ALTER TABLE gts_admission_application
    ADD CONSTRAINT fk_gts_admission_application_desired_stream_tenant
    FOREIGN KEY (tenant_id, desired_stream_id)
    REFERENCES gts_stream (tenant_id, id);

ALTER TABLE gts_admission_application
    ADD CONSTRAINT fk_gts_admission_application_offered_class_grade_tenant
    FOREIGN KEY (tenant_id, offered_class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);

ALTER TABLE gts_admission_application
    ADD CONSTRAINT fk_gts_admission_application_offered_stream_tenant
    FOREIGN KEY (tenant_id, offered_stream_id)
    REFERENCES gts_stream (tenant_id, id);
