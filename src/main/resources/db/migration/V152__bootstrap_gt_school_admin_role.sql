-- GT-EIAM-R1-FIX-001A
-- Bootstrap the missing GT School administrative role.
--
-- Recovery evidence established that migrations V121 onward
-- repeatedly assign permissions to SCHOOL_ADMIN, but no Flyway
-- migration creates that role for the GT-SCHOOL tenant.
--
-- This migration:
--   1. creates the missing role safely;
--   2. preserves an existing role if already present;
--   3. restores the GT Connect Release 1 assignments currently
--      governed by GT-CONNECT-001.
--
-- Historical permission backfill for earlier school capabilities
-- will be handled separately using explicit permission lists rather
-- than granting SCHOOL_ADMIN every tenant permission.


-- ============================================================
-- GT SCHOOL ADMINISTRATIVE ROLE
-- ============================================================

INSERT INTO eiam_role (
    id,
    tenant_id,
    code,
    name,
    description,
    system_role,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    t.id,
    'SCHOOL_ADMIN',
    'School Administrator',
    'Baseline GT School administrative role for governed Release 1 institutional administration.',
    true,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
WHERE t.code = 'GT-SCHOOL'
ON CONFLICT DO NOTHING;


-- ============================================================
-- RESTORE GT CONNECT RELEASE 1 PERMISSION ASSIGNMENTS
-- ============================================================

INSERT INTO eiam_role_permission (
    id,
    tenant_id,
    role_id,
    permission_id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    t.id,
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
JOIN eiam_role r
    ON r.tenant_id = t.id
   AND r.code = 'SCHOOL_ADMIN'
JOIN eiam_permission p
    ON p.tenant_id = t.id
WHERE t.code = 'GT-SCHOOL'
AND p.code IN (
    'core.connect.manage',
    'core.connect.moderate',
    'core.announcements.send'
)
ON CONFLICT DO NOTHING;
