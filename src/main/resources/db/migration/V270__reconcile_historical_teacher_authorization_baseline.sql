-- GT Enterprise Platform
-- V270 — Reconcile historical GT School TEACHER authorization baseline.
--
-- V269 established the intended minimum Teacher baseline:
--
--   school.academic.curriculum.read
--   school.academic.class-grade.read
--   school.academic.subject.read
--   school.academic.teaching-assignment.read
--   ai.request.create
--   ai.request.read
--   ai.runtime.execute
--
-- V269 created the three EAIF definitions tenant-wide but relied on earlier
-- GT School migrations for the four academic definitions. Historical and
-- test-created tenants demonstrate that those academic definitions are not
-- present tenant-wide.
--
-- V270 therefore reconciles the intended baseline for historical tenants.
--
-- Governance:
--   * preserve an existing TEACHER role when present;
--   * create only missing canonical permission definitions;
--   * grant only the seven baseline authorities to TEACHER;
--   * assign no users to TEACHER;
--   * grant no authority to administrator roles;
--   * configure no provider, model, connector, credential or runtime switch.

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
    tenant.id,
    'TEACHER',
    'Teacher',
    'GT School teacher role for governed teacher-facing capabilities.',
    FALSE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V270',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V270',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_role existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = 'TEACHER'
);

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
    definition.code,
    definition.name,
    definition.module,
    definition.description,
    definition.system_permission,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V270',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V270',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
CROSS JOIN (
    VALUES
        (
            'school.academic.curriculum.read',
            'Read Curriculum',
            'SCHOOL_ACADEMIC',
            'Allows viewing curricula',
            FALSE
        ),
        (
            'school.academic.class-grade.read',
            'Read Class Grades',
            'SCHOOL_ACADEMIC',
            'Allows viewing academic class grades',
            FALSE
        ),
        (
            'school.academic.subject.read',
            'Read Subjects',
            'SCHOOL_ACADEMIC',
            'Allows viewing academic subjects',
            FALSE
        ),
        (
            'school.academic.teaching-assignment.read',
            'Read Teaching Assignments',
            'SCHOOL_ACADEMIC',
            'Allows viewing teacher academic assignments',
            FALSE
        ),
        (
            'ai.request.create',
            'AI Request Create',
            'EAIF',
            'Create governed enterprise AI requests.',
            TRUE
        ),
        (
            'ai.request.read',
            'AI Request Read',
            'EAIF',
            'Read governed enterprise AI request state and results.',
            TRUE
        ),
        (
            'ai.runtime.execute',
            'AI Runtime Execute',
            'EAIF',
            'Execute an authorised governed enterprise AI request.',
            TRUE
        )
) AS definition (
    code,
    name,
    module,
    description,
    system_permission
)
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_permission existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = definition.code
);

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
    'GT-MIGRATION-V270',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V270',
    0,
    'ACTIVE'
FROM eiam_role role
JOIN eiam_permission permission
  ON permission.tenant_id = role.tenant_id
WHERE role.code = 'TEACHER'
  AND role.status = 'ACTIVE'
  AND permission.status = 'ACTIVE'
  AND permission.code IN (
      'school.academic.curriculum.read',
      'school.academic.class-grade.read',
      'school.academic.subject.read',
      'school.academic.teaching-assignment.read',
      'ai.request.create',
      'ai.request.read',
      'ai.runtime.execute'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM eiam_role_permission existing
      WHERE existing.tenant_id = role.tenant_id
        AND existing.role_id = role.id
        AND existing.permission_id = permission.id
  );
