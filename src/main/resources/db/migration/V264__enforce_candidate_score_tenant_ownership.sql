ALTER TABLE gts_candidate_paper_registration
    ADD CONSTRAINT uq_gts_candidate_paper_registration_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_mark_entry_batch
    ADD CONSTRAINT uq_gts_mark_entry_batch_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT uq_gts_candidate_score_tenant_id
    UNIQUE (tenant_id, id);

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT fk_gts_candidate_score_mark_sheet_tenant
    FOREIGN KEY (tenant_id, mark_sheet_id)
    REFERENCES gts_mark_sheet (tenant_id, id)
    ON DELETE CASCADE;

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT fk_gts_candidate_score_mark_entry_batch_tenant
    FOREIGN KEY (tenant_id, mark_entry_batch_id)
    REFERENCES gts_mark_entry_batch (tenant_id, id);

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT fk_gts_candidate_score_examination_candidate_tenant
    FOREIGN KEY (tenant_id, examination_candidate_id)
    REFERENCES gts_examination_candidate (tenant_id, id);

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT fk_gts_candidate_score_candidate_paper_registration_tenant
    FOREIGN KEY (tenant_id, candidate_paper_registration_id)
    REFERENCES gts_candidate_paper_registration (tenant_id, id);

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT fk_gts_candidate_score_student_tenant
    FOREIGN KEY (tenant_id, student_id)
    REFERENCES gts_student (tenant_id, id);

ALTER TABLE gts_candidate_score
    ADD CONSTRAINT fk_gts_candidate_score_student_enrollment_tenant
    FOREIGN KEY (tenant_id, student_enrollment_id)
    REFERENCES gts_student_enrollment (tenant_id, id);

ALTER TABLE gts_mark_entry_batch
    ADD CONSTRAINT fk_gts_mark_entry_batch_mark_sheet_tenant
    FOREIGN KEY (tenant_id, mark_sheet_id)
    REFERENCES gts_mark_sheet (tenant_id, id)
    ON DELETE CASCADE;
