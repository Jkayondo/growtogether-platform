-- GT School
-- Campus -> School Profile tenant ownership enforcement
--
-- Defence in depth:
-- Application services already validate School Profile ownership.
-- This migration additionally prevents PostgreSQL from accepting
-- a Campus whose school_profile_id belongs to another tenant.


-- ============================================================
-- TENANT-SAFE SCHOOL PROFILE REFERENCE FOUNDATION
-- ============================================================

ALTER TABLE gts_school_profile
    ADD CONSTRAINT uq_gts_school_profile_tenant_id
        UNIQUE (tenant_id, id);


-- ============================================================
-- CAMPUS -> SCHOOL PROFILE TENANT OWNERSHIP
-- ============================================================

ALTER TABLE gts_campus
    ADD CONSTRAINT fk_gts_campus_school_profile_tenant
        FOREIGN KEY (tenant_id, school_profile_id)
        REFERENCES gts_school_profile (tenant_id, id);
