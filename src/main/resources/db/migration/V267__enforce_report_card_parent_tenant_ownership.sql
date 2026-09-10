-- GT School Report Card tenant-parent ownership hardening.
--
-- Preserve the existing ID-only foreign keys.
-- Add composite tenant ownership so a report card cannot reference
-- a parent record belonging to another tenant.

ALTER TABLE gts_report_card_template
    ADD CONSTRAINT uq_gts_report_card_template_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_student_term_result
    ADD CONSTRAINT uq_gts_student_term_result_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_template
    FOREIGN KEY (tenant_id, report_card_template_id)
    REFERENCES gts_report_card_template (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_student
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_enrollment
    FOREIGN KEY (tenant_id, student_enrollment_id)
    REFERENCES gts_student_enrollment (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_term_result
    FOREIGN KEY (tenant_id, student_term_result_id)
    REFERENCES gts_student_term_result (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_academic_year
    FOREIGN KEY (tenant_id, academic_year_id)
    REFERENCES gts_academic_year (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_academic_term
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_class_grade
    FOREIGN KEY (tenant_id, class_grade_id)
    REFERENCES gts_class_grade (tenant_id, id);

ALTER TABLE gts_report_card
    ADD CONSTRAINT fk_gts_report_card_tenant_stream
    FOREIGN KEY (tenant_id, stream_id)
    REFERENCES gts_stream (tenant_id, id);
