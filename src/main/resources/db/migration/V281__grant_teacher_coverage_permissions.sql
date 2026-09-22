-- GT Enterprise Platform
-- V281 — Grant dedicated GT School Teacher Coverage permissions.
--
-- Authority:
--   HQ-GS-TEACHER-NEXT-001 — APPROVED
--   GT-SCH-TEACHER-COVERAGE-CONTRACT-001
--
-- Permissions:
--
--   school.teacher.coverage.read
--   school.teacher.coverage.update
--
-- Security boundaries:
--   * permissions apply to authenticated teacher self-service only;
--   * teacher and tenant identity remain server-derived;
--   * no create/delete/reset authority is granted;
--   * no authority to inspect or mutate another teacher's coverage is granted;
--   * no curriculum-administration or teaching-assignment-management authority
--     is implied.
--
-- Existing tenants receive the permissions on active TEACHER roles.
-- New tenants receive the same definitions from TenantProvisioningService.

WITH permission_seed (
    code,
    name,
    description
) AS (
    VALUES
        (
            'school.teacher.coverage.read',
            'Read Teacher Coverage',
            'Allows an authenticated teacher to view curriculum coverage belonging to their own authorised teaching assignments within their authenticated tenant.'
        ),
        (
            'school.teacher.coverage.update',
            'Update Teacher Coverage',
            'Allows an authenticated teacher to update curriculum coverage status only for records belonging to their own authorised teaching assignments within their authenticated tenant.'
        )
)
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
    seed.code,
    seed.name,
    'SCHOOL_TEACHER',
    seed.description,
    FALSE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V281',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V281',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
CROSS JOIN permission_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_permission existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = seed.code
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
    'GT-MIGRATION-V281',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V281',
    0,
    'ACTIVE'
FROM eiam_role role
JOIN eiam_permission permission
  ON permission.tenant_id = role.tenant_id
WHERE role.code = 'TEACHER'
  AND role.status = 'ACTIVE'
  AND permission.status = 'ACTIVE'
  AND permission.code IN (
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
