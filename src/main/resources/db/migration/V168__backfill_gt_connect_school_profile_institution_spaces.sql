-- ============================================================
-- GT CONNECT — EXISTING SCHOOL PROFILE INSTITUTION SPACE BACKFILL
-- ============================================================
--
-- V167 established the canonical uniqueness rule:
--
--   tenant
--     + INSTITUTION
--     + SCHOOL_PROFILE
--     + SchoolProfile.id
--
-- New School Profiles are provisioned by
-- SchoolConnectSpaceProvisioningService.
--
-- This migration provisions only the missing canonical spaces
-- for School Profiles that existed before that integration.
-- ============================================================

INSERT INTO gt_connect_spaces (
    id,
    tenant_id,
    space_type,
    name,
    context_type,
    context_reference,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    school.tenant_id,
    'INSTITUTION',
    school.school_name,
    'SCHOOL_PROFILE',
    school.id::text,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM gts_school_profile school
WHERE NOT EXISTS (
    SELECT 1
    FROM gt_connect_spaces space
    WHERE space.tenant_id = school.tenant_id
      AND space.space_type = 'INSTITUTION'
      AND space.context_type = 'SCHOOL_PROFILE'
      AND space.context_reference = school.id::text
)
ON CONFLICT DO NOTHING;
