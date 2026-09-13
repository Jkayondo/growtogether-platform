-- Seed GT School assessment permissions

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
        'school.academic.assessment.create',
        'Create Assessments',
        'SCHOOL_ACADEMIC',
        'Allows creation of academic assessments'
    ),
    (
        'school.academic.assessment.read',
        'Read Assessments',
        'SCHOOL_ACADEMIC',
        'Allows viewing academic assessments'
    ),
    (
        'school.academic.assessment.manage',
        'Manage Assessments',
        'SCHOOL_ACADEMIC',
        'Allows archiving and management of academic assessments'
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
    'school.academic.assessment.create',
    'school.academic.assessment.read',
    'school.academic.assessment.manage'
)
ON CONFLICT DO NOTHING;
