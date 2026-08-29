-- Seed Enterprise Workforce permissions for the GT School Release 1 tenant.
--
-- Enterprise Workforce is a shared platform capability.
-- The permissions therefore use the enterprise.workforce namespace
-- rather than the school.academic namespace.

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
    p.name,
    p.module,
    p.description,
    false,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
CROSS JOIN (
    VALUES
    (
        'enterprise.workforce.member.create',
        'Create Workforce Member',
        'ENTERPRISE_WORKFORCE',
        'Allows creation of enterprise workforce members'
    ),
    (
        'enterprise.workforce.member.read',
        'Read Workforce Members',
        'ENTERPRISE_WORKFORCE',
        'Allows viewing enterprise workforce members'
    ),
    (
        'enterprise.workforce.member.manage',
        'Manage Workforce Members',
        'ENTERPRISE_WORKFORCE',
        'Allows management of enterprise workforce members'
    )
) AS p(
    code,
    name,
    module,
    description
)
WHERE t.code = 'GT-SCHOOL'
ON CONFLICT DO NOTHING;


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
    'enterprise.workforce.member.create',
    'enterprise.workforce.member.read',
    'enterprise.workforce.member.manage'
)
ON CONFLICT DO NOTHING;
