-- GT School
-- Enforce tenant ownership between Student and Admission Application.

ALTER TABLE gts_admission_application
    ADD CONSTRAINT uq_gts_admission_application_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_student
    ADD CONSTRAINT fk_gts_student_admission_application_tenant
    FOREIGN KEY (tenant_id, admission_application_id)
    REFERENCES gts_admission_application (tenant_id, id);
