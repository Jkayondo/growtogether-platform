-- GT School Release 1
-- Parent / Guardian EIAM role bootstrap.
--
-- A12.5 Parent Account Provisioning requires a stable EIAM role
-- that can be carried by the secure OrganizationInvitation lifecycle.
--
-- IMPORTANT:
-- No permissions are assigned by this migration.
-- Parent-facing permissions must be introduced explicitly alongside
-- verified parent-facing capabilities and endpoints.
--
-- This preserves least privilege and avoids granting administrative
-- guardian-management permissions to parents.

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
    'PARENT',
    'Parent / Guardian',
    'Baseline GT School parent and guardian identity role for secure account provisioning and future governed parent-facing capabilities.',
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
