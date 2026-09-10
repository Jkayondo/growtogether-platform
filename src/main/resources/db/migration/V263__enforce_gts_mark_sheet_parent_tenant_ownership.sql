-- ---------------------------------------------------------------------------
-- GT School — Mark Sheet parent tenant ownership
--
-- Hardens gts_mark_sheet so every referenced School-domain parent belongs
-- to the same tenant as the mark sheet.
--
-- Existing single-column foreign keys are deliberately retained.
-- ---------------------------------------------------------------------------


-- Required composite parent key for Assessment Component.
ALTER TABLE gts_assessment_component
    ADD CONSTRAINT uq_gts_assessment_component_tenant_id
        UNIQUE (tenant_id, id);


-- Prepare Mark Sheet for tenant-safe downstream references
-- such as Candidate Score.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT uq_gts_mark_sheet_tenant_id
        UNIQUE (tenant_id, id);


-- Assessment Component must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_component_tenant
        FOREIGN KEY (
            tenant_id,
            assessment_component_id
        )
        REFERENCES gts_assessment_component (
            tenant_id,
            id
        );


-- Optional Assessment Paper must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_paper_tenant
        FOREIGN KEY (
            tenant_id,
            assessment_paper_id
        )
        REFERENCES gts_assessment_paper (
            tenant_id,
            id
        );


-- Optional Examination Schedule must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_schedule_tenant
        FOREIGN KEY (
            tenant_id,
            examination_schedule_id
        )
        REFERENCES gts_examination_schedule (
            tenant_id,
            id
        );


-- Subject Offering must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_subject_offering_tenant
        FOREIGN KEY (
            tenant_id,
            subject_offering_id
        )
        REFERENCES gts_subject_offering (
            tenant_id,
            id
        );


-- Class Offering must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_class_offering_tenant
        FOREIGN KEY (
            tenant_id,
            class_offering_id
        )
        REFERENCES gts_class_offering (
            tenant_id,
            id
        );


-- Optional Stream must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_stream_tenant
        FOREIGN KEY (
            tenant_id,
            stream_id
        )
        REFERENCES gts_stream (
            tenant_id,
            id
        );


-- Optional Teacher Profile must belong to the Mark Sheet tenant.
ALTER TABLE gts_mark_sheet
    ADD CONSTRAINT fk_gts_mark_sheet_teacher_profile_tenant
        FOREIGN KEY (
            tenant_id,
            teacher_profile_id
        )
        REFERENCES gts_teacher_profile (
            tenant_id,
            id
        );
