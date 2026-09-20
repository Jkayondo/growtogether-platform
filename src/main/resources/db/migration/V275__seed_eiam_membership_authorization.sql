-- GT-CONNECT-PILOT-DEFECT-001
-- Controlled Release 1 repair for missing EIAM invitation and
-- tenant-membership authorization definitions.
--
-- Scope:
--   * GT-SCHOOL only.
--   * Define the six authorities already required by MembershipController.
--   * Assign them only to TENANT_ADMIN.
--   * Do not widen PARENT, SCHOOL_ADMIN or INTEGRATION_ADMIN authority.
--
-- This migration creates authorization metadata only.
-- It does not create invitations, users, memberships or Connect spaces.

WITH required_permission (
    code,
    name,
    description
) AS (
    VALUES
        (
            'eiam.invitations.create',
            'Create EIAM Invitations',
            'Allows governed creation of tenant-scoped organization invitations.'
        ),
        (
            'eiam.invitations.read',
            'Read EIAM Invitations',
            'Allows governed reading of tenant-scoped organization invitations.'
        ),
        (
            'eiam.invitations.resend',
            'Resend EIAM Invitations',
            'Allows governed re-issuance of tenant-scoped organization invitations.'
        ),
        (
            'eiam.invitations.revoke',
            'Revoke EIAM Invitations',
            'Allows governed revocation of tenant-scoped organization invitations.'
        ),
        (
            'eiam.memberships.read',
            'Read EIAM Memberships',
            'Allows governed reading of tenant membership records.'
        ),
        (
            'eiam.memberships.update',
            'Update EIAM Memberships',
            'Allows governed tenant membership lifecycle updates.'
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
    required.code,
    required.name,
    'EIAM',
    required.description,
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
CROSS JOIN required_permission required
WHERE tenant.code = 'GT-SCHOOL'
ON CONFLICT (tenant_id, code)
DO NOTHING;


-- ============================================================
-- TENANT_ADMIN GOVERNED ASSIGNMENT
-- ============================================================

WITH required_permission (code) AS (
    VALUES
        ('eiam.invitations.create'),
        ('eiam.invitations.read'),
        ('eiam.invitations.resend'),
        ('eiam.invitations.revoke'),
        ('eiam.memberships.read'),
        ('eiam.memberships.update')
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
    role.tenant_id,
    role.id,
    permission.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
JOIN eiam_role role
  ON role.tenant_id = tenant.id
 AND role.code = 'TENANT_ADMIN'
 AND role.status = 'ACTIVE'
JOIN required_permission required
  ON TRUE
JOIN eiam_permission permission
  ON permission.tenant_id = tenant.id
 AND permission.code = required.code
 AND permission.status = 'ACTIVE'
WHERE tenant.code = 'GT-SCHOOL'
ON CONFLICT (
    tenant_id,
    role_id,
    permission_id
)
DO NOTHING;
