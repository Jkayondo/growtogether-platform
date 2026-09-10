-- GT School
-- Examination Session parent tenant ownership hardening.
--
-- Application validation already requires Academic Year,
-- optional Academic Term and Campus to belong to the
-- authenticated tenant.
--
-- This migration adds equivalent PostgreSQL protection so
-- direct database writes cannot associate an examination
-- session with parent records belonging to another tenant.
--
-- Existing ID-only foreign keys are intentionally retained.
-- These composite constraints add tenant ownership rather
-- than replace the original referential relationships.


ALTER TABLE gts_examination_session
    ADD CONSTRAINT fk_gts_examination_session_academic_year_tenant
    FOREIGN KEY (
        tenant_id,
        academic_year_id
    )
    REFERENCES gts_academic_year (
        tenant_id,
        id
    );


ALTER TABLE gts_examination_session
    ADD CONSTRAINT fk_gts_examination_session_academic_term_tenant
    FOREIGN KEY (
        tenant_id,
        academic_term_id
    )
    REFERENCES gts_academic_term (
        tenant_id,
        id
    );


ALTER TABLE gts_examination_session
    ADD CONSTRAINT fk_gts_examination_session_campus_tenant
    FOREIGN KEY (
        tenant_id,
        campus_id
    )
    REFERENCES gts_campus (
        tenant_id,
        id
    );
