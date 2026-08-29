-- GT School Release 1
-- A11.2 — Timetable governance permissions
--
-- IMPROVEMENT:
-- Consequential timetable lifecycle authorities are deliberately
-- separated from ordinary timetable management.

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
        'school.timetable.create',
        'Create Timetable',
        'SCHOOL_TIMETABLE',
        'Allows creation of school timetables'
    ),
    (
        'school.timetable.read',
        'Read Timetables',
        'SCHOOL_TIMETABLE',
        'Allows viewing school timetables and timetable details'
    ),
    (
        'school.timetable.manage',
        'Manage Timetables',
        'SCHOOL_TIMETABLE',
        'Allows ordinary management and editing of timetable structures'
    ),
    (
        'school.timetable.review',
        'Submit Timetable for Review',
        'SCHOOL_TIMETABLE',
        'Allows a timetable to be submitted into the governed human review lifecycle'
    ),
    (
        'school.timetable.approve',
        'Approve Timetable',
        'SCHOOL_TIMETABLE',
        'Allows authorised human approval of a reviewed timetable'
    ),
    (
        'school.timetable.publish',
        'Publish Timetable',
        'SCHOOL_TIMETABLE',
        'Allows publication of an approved timetable'
    ),
    (
        'school.timetable.activate',
        'Activate Timetable',
        'SCHOOL_TIMETABLE',
        'Allows activation of a published timetable for academic operations'
    ),
    (
        'school.timetable.suspend',
        'Suspend Timetable',
        'SCHOOL_TIMETABLE',
        'Allows suspension of an active timetable'
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
    'school.timetable.create',
    'school.timetable.read',
    'school.timetable.manage',
    'school.timetable.review',
    'school.timetable.approve',
    'school.timetable.publish',
    'school.timetable.activate',
    'school.timetable.suspend'
)
ON CONFLICT DO NOTHING;
