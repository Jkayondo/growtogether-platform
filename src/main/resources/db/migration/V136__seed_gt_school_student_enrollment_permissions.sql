-- GT School Release 1
-- Student / Learner Profile and Class Enrollment permissions

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
        'school.student.create',
        'Create Student',
        'SCHOOL_STUDENT',
        'Allows creation of GT School learner/student profiles'
    ),
    (
        'school.student.read',
        'Read Students',
        'SCHOOL_STUDENT',
        'Allows viewing GT School learner/student profiles'
    ),
    (
        'school.student.manage',
        'Manage Students',
        'SCHOOL_STUDENT',
        'Allows management of GT School learner/student profiles'
    ),
    (
        'school.enrollment.create',
        'Create Student Enrollment',
        'SCHOOL_ACADEMIC',
        'Allows enrollment of learners into academic structures'
    ),
    (
        'school.enrollment.read',
        'Read Student Enrollments',
        'SCHOOL_ACADEMIC',
        'Allows viewing learner enrollment records'
    ),
    (
        'school.enrollment.manage',
        'Manage Student Enrollments',
        'SCHOOL_ACADEMIC',
        'Allows management of learner enrollment lifecycle'
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
    'school.student.create',
    'school.student.read',
    'school.student.manage',
    'school.enrollment.create',
    'school.enrollment.read',
    'school.enrollment.manage'
)
ON CONFLICT DO NOTHING;
