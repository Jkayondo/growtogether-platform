-- GT School
-- Candidate Paper Registration parent tenant ownership hardening.
--
-- Candidate Paper Registration already carries tenant_id, but the
-- original V041 parent foreign keys referenced parent IDs only.
--
-- This migration adds tenant-aware parent candidate keys and
-- composite foreign keys so a registration cannot reference a
-- Candidate, Assessment Paper, or Examination Schedule belonging
-- to another tenant.

ALTER TABLE gts_examination_candidate
    ADD CONSTRAINT uq_gts_examination_candidate_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_assessment_paper
    ADD CONSTRAINT uq_gts_assessment_paper_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_examination_schedule
    ADD CONSTRAINT uq_gts_examination_schedule_tenant_id
        UNIQUE (tenant_id, id);


ALTER TABLE gts_candidate_paper_registration
    ADD CONSTRAINT fk_gts_candidate_paper_registration_candidate_tenant
        FOREIGN KEY (
            tenant_id,
            examination_candidate_id
        )
        REFERENCES gts_examination_candidate (
            tenant_id,
            id
        )
        ON DELETE CASCADE;


ALTER TABLE gts_candidate_paper_registration
    ADD CONSTRAINT fk_gts_candidate_paper_registration_paper_tenant
        FOREIGN KEY (
            tenant_id,
            assessment_paper_id
        )
        REFERENCES gts_assessment_paper (
            tenant_id,
            id
        );


ALTER TABLE gts_candidate_paper_registration
    ADD CONSTRAINT fk_gts_candidate_paper_registration_schedule_tenant
        FOREIGN KEY (
            tenant_id,
            examination_schedule_id
        )
        REFERENCES gts_examination_schedule (
            tenant_id,
            id
        );
