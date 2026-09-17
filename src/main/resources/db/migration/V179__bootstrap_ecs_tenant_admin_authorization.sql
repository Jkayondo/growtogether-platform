-- GrowTogether Enterprise Configuration Service
-- Tenant administration authorization bootstrap and historical recovery.
--
-- Objectives:
--   1. Seed the canonical TENANT_ADMIN bootstrap authorities.
--   2. Seed ECS configuration authorities.
--   3. Ensure historical tenants have the TENANT_ADMIN system role.
--   4. Assign normal tenant-management/configuration authorities to it.
--   5. Keep platform.configuration.secret.read unassigned by default.
--   6. Recover a historical administrator only where ownership is
--      unambiguous: exactly one ACTIVE user holds both SCHOOL_ADMIN
--      and INTEGRATION_ADMIN.
--
-- This migration does NOT enable external provider delivery.
-- This migration does NOT dispatch notifications.
-- This migration does NOT grant configuration-secret read authority.

-- =========================================================
-- PERMISSION DEFINITIONS
-- =========================================================

WITH permission_code(code) AS (
    VALUES
        ('eiam.users.create'),
        ('eiam.users.read'),
        ('eiam.users.update'),
        ('eiam.users.activate'),
        ('eiam.users.suspend'),
        ('eiam.users.deactivate'),

        ('eiam.roles.create'),
        ('eiam.roles.read'),
        ('eiam.roles.update'),
        ('eiam.roles.delete'),

        ('eiam.user-roles.assign'),
        ('eiam.user-roles.read'),

        ('eiam.permissions.create'),
        ('eiam.permissions.read'),
        ('eiam.permissions.update'),
        ('eiam.permissions.delete'),

        ('eiam.role-permissions.assign'),
        ('eiam.role-permissions.read'),

        ('platform.tenants.read'),
        ('platform.tenants.manage'),

        ('platform.configuration.definition.manage'),
        ('platform.configuration.manage'),
        ('platform.configuration.read'),
        ('platform.configuration.history.read'),
        ('platform.configuration.rollback'),
        ('platform.configuration.secret.read')
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
    t.id,
    p.code,
    initcap(
        replace(
            replace(
                p.code,
                '.',
                ' '
            ),
            '-',
            ' '
        )
    ),
    upper(
        split_part(
            p.code,
            '.',
            1
        )
    ),
    'GrowTogether governed bootstrap authority for '
        || p.code
        || '.',
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
CROSS JOIN permission_code p
ON CONFLICT (tenant_id, code)
DO NOTHING;


-- =========================================================
-- TENANT_ADMIN ROLE
-- =========================================================

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
    'TENANT_ADMIN',
    'Tenant Administrator',
    'Bootstrap administrator with tenant-wide governed authority.',
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_role r
    WHERE r.tenant_id = t.id
      AND upper(r.code) = 'TENANT_ADMIN'
)
ON CONFLICT DO NOTHING;


-- =========================================================
-- TENANT_ADMIN AUTHORITY ASSIGNMENT
--
-- platform.configuration.secret.read is deliberately excluded.
-- =========================================================

WITH tenant_admin_permission(code) AS (
    VALUES
        ('eiam.users.create'),
        ('eiam.users.read'),
        ('eiam.users.update'),
        ('eiam.users.activate'),
        ('eiam.users.suspend'),
        ('eiam.users.deactivate'),

        ('eiam.roles.create'),
        ('eiam.roles.read'),
        ('eiam.roles.update'),
        ('eiam.roles.delete'),

        ('eiam.user-roles.assign'),
        ('eiam.user-roles.read'),

        ('eiam.permissions.create'),
        ('eiam.permissions.read'),
        ('eiam.permissions.update'),
        ('eiam.permissions.delete'),

        ('eiam.role-permissions.assign'),
        ('eiam.role-permissions.read'),

        ('platform.tenants.read'),
        ('platform.tenants.manage'),

        ('platform.configuration.manage'),
        ('platform.configuration.read'),
        ('platform.configuration.history.read'),
        ('platform.configuration.rollback')
)
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
    r.tenant_id,
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_role r
JOIN tenant_admin_permission required
  ON TRUE
JOIN eiam_permission p
  ON p.tenant_id = r.tenant_id
 AND p.code = required.code
 AND p.status = 'ACTIVE'
WHERE r.code = 'TENANT_ADMIN'
  AND r.status = 'ACTIVE'
ON CONFLICT (
    tenant_id,
    role_id,
    permission_id
)
DO NOTHING;


-- =========================================================
-- HISTORICAL ADMINISTRATOR RECOVERY
--
-- Only recover automatically when:
--   * TENANT_ADMIN currently has no ACTIVE assignee; and
--   * exactly one ACTIVE user holds BOTH SCHOOL_ADMIN
--     and INTEGRATION_ADMIN.
--
-- Ambiguous tenants are deliberately left untouched.
-- =========================================================

WITH administrative_candidates AS (
    SELECT
        ur.tenant_id,
        ur.user_id
    FROM eiam_user_role ur
    JOIN eiam_user_account u
      ON u.id = ur.user_id
     AND u.tenant_id = ur.tenant_id
    JOIN eiam_role r
      ON r.id = ur.role_id
     AND r.tenant_id = ur.tenant_id
    WHERE ur.status = 'ACTIVE'
      AND u.status = 'ACTIVE'
      AND u.account_status = 'ACTIVE'
      AND r.status = 'ACTIVE'
      AND r.code IN (
          'SCHOOL_ADMIN',
          'INTEGRATION_ADMIN'
      )
    GROUP BY
        ur.tenant_id,
        ur.user_id
    HAVING COUNT(
        DISTINCT r.code
    ) = 2
),
unambiguous_administrator AS (
    SELECT
        tenant_id,
        MAX(user_id::text)::uuid AS user_id
    FROM administrative_candidates
    GROUP BY tenant_id
    HAVING COUNT(*) = 1
)
INSERT INTO eiam_user_role (
    id,
    tenant_id,
    user_id,
    role_id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    candidate.tenant_id,
    candidate.user_id,
    tenant_admin.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM unambiguous_administrator candidate
JOIN eiam_role tenant_admin
  ON tenant_admin.tenant_id = candidate.tenant_id
 AND tenant_admin.code = 'TENANT_ADMIN'
 AND tenant_admin.status = 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_user_role existing
    WHERE existing.tenant_id =
          candidate.tenant_id
      AND existing.role_id =
          tenant_admin.id
      AND existing.status = 'ACTIVE'
)
ON CONFLICT (
    tenant_id,
    user_id,
    role_id
)
DO NOTHING;
