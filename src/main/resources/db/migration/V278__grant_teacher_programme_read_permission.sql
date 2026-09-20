-- GT Enterprise Platform
-- V278 — Grant dedicated GT School Teacher Programme read permission.
--
-- Authority:
--   HQ-GS-TEACHER-PROG-PERM-001 — APPROVED
--
-- Purpose:
--   Introduce the dedicated least-privilege authority:
--
--     school.teacher.programme.read
--
--   and grant it to the governed TEACHER role for existing tenants.
--
-- Security boundaries:
--   * does not grant school.academic.calendar.read;
--   * does not grant calendar administration;
--   * does not grant timetable or teaching-assignment modification;
--   * does not assign users to TEACHER;
--   * does not alter tenant or teacher ownership controls;
--   * endpoint identity remains derived from the authenticated GT identity.
--
-- Reconciliation strategy:
--   * create the canonical permission only when absent;
--   * preserve existing TEACHER roles;
--   * grant only this permission to active TEACHER roles when absent;
--   * remain safe to evaluate against historical tenants.

INSERT INTO eiam_permission (
    id,
    tenant_id,
    code,
    name,
    module,
    description,
    system_permission,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    tenant.id,
    'school.teacher.programme.read',
    'Read Teacher Programme',
    'SCHOOL_TEACHER',
    'Allows an authenticated teacher to view their own authorised Today''s Programme, including applicable teaching lessons and teacher-visible school calendar events within their authenticated tenant.',
    FALSE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V278',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V278',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_permission existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = 'school.teacher.programme.read'
);

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
    role.tenant_id,
    role.id,
    permission.id,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V278',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V278',
    0,
    'ACTIVE'
FROM eiam_role role
JOIN eiam_permission permission
  ON permission.tenant_id = role.tenant_id
WHERE role.code = 'TEACHER'
  AND role.status = 'ACTIVE'
  AND permission.code = 'school.teacher.programme.read'
  AND permission.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM eiam_role_permission existing
      WHERE existing.tenant_id = role.tenant_id
        AND existing.role_id = role.id
        AND existing.permission_id = permission.id
  );
