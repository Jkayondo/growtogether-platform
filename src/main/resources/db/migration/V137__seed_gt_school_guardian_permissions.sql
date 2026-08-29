-- GT School Release 1
-- Guardian identity and Student-Guardian Relationship permissions

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
        'school.guardian.create',
        'Create Guardian',
        'SCHOOL_STUDENT',
        'Allows creation of parent or guardian profiles'
    ),
    (
        'school.guardian.read',
        'Read Guardians',
        'SCHOOL_STUDENT',
        'Allows viewing parent or guardian profiles'
    ),
    (
        'school.guardian.manage',
        'Manage Guardians',
        'SCHOOL_STUDENT',
        'Allows management of guardian verification and lifecycle'
    ),
    (
        'school.guardian-relationship.create',
        'Create Student Guardian Relationship',
        'SCHOOL_STUDENT',
        'Allows linking guardians to learners'
    ),
    (
        'school.guardian-relationship.read',
        'Read Student Guardian Relationships',
        'SCHOOL_STUDENT',
        'Allows viewing learner and guardian relationships'
    ),
    (
        'school.guardian-relationship.manage',
        'Manage Student Guardian Relationships',
        'SCHOOL_STUDENT',
        'Allows management of learner and guardian relationship lifecycle'
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
    'school.guardian.create',
    'school.guardian.read',
    'school.guardian.manage',
    'school.guardian-relationship.create',
    'school.guardian-relationship.read',
    'school.guardian-relationship.manage'
)
ON CONFLICT DO NOTHING;
