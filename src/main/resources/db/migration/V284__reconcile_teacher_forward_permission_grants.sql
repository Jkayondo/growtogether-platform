-- GrowTogether School Teacher
-- V284 — Reconcile forward Teacher Programme/Coverage permission grants.
--
-- Step 27 controlled pilot remediation.
--
-- Context:
--   V278 and V281 define the dedicated Teacher Programme/Coverage
--   permissions before the forward-only V282 reconciliation recreates
--   the historical TEACHER baseline when V270 is absent.
--
-- Purpose:
--   After the TEACHER role exists, reconcile only the three already
--   defined least-privilege authorities onto active TEACHER roles.
--
-- Security boundaries:
--   * creates no permission definitions;
--   * creates no roles;
--   * assigns no users to roles;
--   * grants nothing to administrator or non-TEACHER roles;
--   * remains tenant-scoped;
--   * introduces no create/delete/reset authority;
--   * does not broaden curriculum or teaching-assignment administration;
--   * is idempotent by exact tenant/role/permission NOT EXISTS guard.

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
    'GT-MIGRATION-V284',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V284',
    0,
    'ACTIVE'
FROM eiam_role role
JOIN eiam_permission permission
  ON permission.tenant_id = role.tenant_id
WHERE role.code = 'TEACHER'
  AND role.status = 'ACTIVE'
  AND permission.status = 'ACTIVE'
  AND permission.code IN (
      'school.teacher.programme.read',
      'school.teacher.coverage.read',
      'school.teacher.coverage.update'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM eiam_role_permission existing
      WHERE existing.tenant_id = role.tenant_id
        AND existing.role_id = role.id
        AND existing.permission_id = permission.id
  );
