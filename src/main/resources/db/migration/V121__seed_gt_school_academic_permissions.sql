-- Seed GT School academic permissions

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
        'school.academic.year.create',
        'Create Academic Year',
        'SCHOOL_ACADEMIC',
        'Allows creation of academic years'
    ),
    (
        'school.academic.year.read',
        'Read Academic Years',
        'SCHOOL_ACADEMIC',
        'Allows viewing academic years'
    ),
    (
        'school.academic.term.create',
        'Create Academic Term',
        'SCHOOL_ACADEMIC',
        'Allows creation of academic terms'
    ),
    (
        'school.academic.term.read',
        'Read Academic Terms',
        'SCHOOL_ACADEMIC',
        'Allows viewing academic terms'
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
    ON r.code = 'SCHOOL_ADMIN'
JOIN eiam_permission p
    ON p.tenant_id = t.id
WHERE t.code = 'GT-SCHOOL'
AND p.code IN (
    'school.academic.year.create',
    'school.academic.year.read',
    'school.academic.term.create',
    'school.academic.term.read'
)
ON CONFLICT DO NOTHING;
