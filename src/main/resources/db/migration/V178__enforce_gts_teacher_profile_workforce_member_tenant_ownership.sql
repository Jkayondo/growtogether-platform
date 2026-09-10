-- GT-SCH-TEACHER-PROFILE-WORKFORCE-DB-INTEGRITY-001
--
-- Additive tenant-isolation hardening for the
-- Teacher Profile -> Workforce Member relationship.
--
-- Existing single-column foreign keys are intentionally retained.
-- Existing data was audited before this migration:
--   * 0 Teacher Profiles existed;
--   * 0 cross-tenant Workforce Member links were found.


-- ============================================================
-- Workforce Member parent candidate key
-- ============================================================

ALTER TABLE ewf_workforce_member
    ADD CONSTRAINT uq_ewf_workforce_member_tenant_id
    UNIQUE (tenant_id, id);


-- ============================================================
-- Teacher Profile tenant-aware Workforce Member foreign key
-- ============================================================

ALTER TABLE gts_teacher_profile
    ADD CONSTRAINT fk_gts_teacher_profile_workforce_member_tenant
    FOREIGN KEY (tenant_id, workforce_member_id)
    REFERENCES ewf_workforce_member (tenant_id, id);
