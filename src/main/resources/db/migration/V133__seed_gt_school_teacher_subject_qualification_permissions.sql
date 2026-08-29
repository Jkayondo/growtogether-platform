-- Seed GT School teacher subject qualification permissions

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
        'school.academic.teacher-subject-qualification.create',
        'Create Teacher Subject Qualification',
        'SCHOOL_ACADEMIC',
        'Allows creation of teacher subject qualifications'
    ),
    (
        'school.academic.teacher-subject-qualification.read',
        'Read Teacher Subject Qualifications',
        'SCHOOL_ACADEMIC',
        'Allows viewing teacher subject qualifications'
    ),
    (
        'school.academic.teacher-subject-qualification.manage',
        'Manage Teacher Subject Qualifications',
        'SCHOOL_ACADEMIC',
        'Allows verification and management of teacher subject qualifications'
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
    'school.academic.teacher-subject-qualification.create',
    'school.academic.teacher-subject-qualification.read',
    'school.academic.teacher-subject-qualification.manage'
)
ON CONFLICT DO NOTHING;
