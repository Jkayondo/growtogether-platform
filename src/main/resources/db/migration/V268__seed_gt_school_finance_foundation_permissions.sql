-- GT School Finance Foundation permissions.
-- FIN-B1 application authority baseline.

WITH target_tenant AS (
    SELECT id
    FROM eiam_tenant
    WHERE code = 'GT-SCHOOL'

    UNION

    SELECT DISTINCT tenant_id
    FROM gts_school_profile
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
FROM target_tenant t
CROSS JOIN (
    VALUES
    (
        'school.finance.read',
        'Read School Finance',
        'SCHOOL_FINANCE',
        'Allows viewing school finance configuration and learner financial accounts'
    ),
    (
        'school.finance.manage',
        'Manage School Finance',
        'SCHOOL_FINANCE',
        'Allows maintaining fee categories, fee items, fee structures and learner financial accounts'
    ),
    (
        'school.finance.approve',
        'Approve School Finance Configuration',
        'SCHOOL_FINANCE',
        'Allows authorised approval and activation of school fee structures'
    )
) AS p(
    code,
    name,
    module,
    description
)
ON CONFLICT DO NOTHING;


WITH target_tenant AS (
    SELECT id
    FROM eiam_tenant
    WHERE code = 'GT-SCHOOL'

    UNION

    SELECT DISTINCT tenant_id
    FROM gts_school_profile
),
intended_permission(code) AS (
    VALUES
        ('school.finance.read'),
        ('school.finance.manage'),
        ('school.finance.approve')
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
    t.id,
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM target_tenant t
JOIN eiam_role r
  ON r.tenant_id = t.id
 AND r.code IN (
     'SCHOOL_ADMIN',
     'FINANCE_OFFICER'
 )
JOIN intended_permission intended
  ON TRUE
JOIN eiam_permission p
  ON p.tenant_id = t.id
 AND p.code = intended.code
ON CONFLICT DO NOTHING;
