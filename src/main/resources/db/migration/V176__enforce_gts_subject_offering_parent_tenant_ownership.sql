-- GT School Subject Offering parent tenant ownership
--
-- Additive hardening:
--   * preserve existing single-column foreign keys;
--   * add (tenant_id, id) candidate keys to Subject Offering parents;
--   * add composite tenant-aware foreign keys from gts_subject_offering.
--
-- Existing data was audited before this migration and contained no
-- cross-tenant Subject Offering parent references.


-- ============================================================
-- Parent candidate keys
-- ============================================================

ALTER TABLE gts_class_offering
    ADD CONSTRAINT uq_gts_class_offering_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE gts_academic_term
    ADD CONSTRAINT uq_gts_academic_term_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE gts_stream
    ADD CONSTRAINT uq_gts_stream_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE gts_subject
    ADD CONSTRAINT uq_gts_subject_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE gts_academic_department
    ADD CONSTRAINT uq_gts_academic_department_tenant_id
    UNIQUE (tenant_id, id);


ALTER TABLE gts_grading_scheme
    ADD CONSTRAINT uq_gts_grading_scheme_tenant_id
    UNIQUE (tenant_id, id);


-- ============================================================
-- Subject Offering tenant-aware parent foreign keys
-- ============================================================

ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_class_offering_tenant
    FOREIGN KEY (tenant_id, class_offering_id)
    REFERENCES gts_class_offering (tenant_id, id)
    ON DELETE CASCADE;


ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_academic_term_tenant
    FOREIGN KEY (tenant_id, academic_term_id)
    REFERENCES gts_academic_term (tenant_id, id);


ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_stream_tenant
    FOREIGN KEY (tenant_id, stream_id)
    REFERENCES gts_stream (tenant_id, id);


ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_subject_tenant
    FOREIGN KEY (tenant_id, subject_id)
    REFERENCES gts_subject (tenant_id, id);


ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_department_tenant
    FOREIGN KEY (tenant_id, academic_department_id)
    REFERENCES gts_academic_department (tenant_id, id);


ALTER TABLE gts_subject_offering
    ADD CONSTRAINT fk_gts_subject_offering_grading_scheme_tenant
    FOREIGN KEY (tenant_id, grading_scheme_id)
    REFERENCES gts_grading_scheme (tenant_id, id);
