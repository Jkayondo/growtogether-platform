-- GT Enterprise Platform
-- V269 — Bootstrap GT School TEACHER role and governed Teacher AI access.
--
-- Earlier GT School migrations seed academic permission definitions but
-- do not bootstrap an EIAM TEACHER role.
--
-- Existing development environments may already contain a TEACHER role;
-- therefore this migration preserves an existing role and only creates
-- the role when absent.
--
-- Minimum Teacher baseline:
--   school.academic.curriculum.read
--   school.academic.class-grade.read
--   school.academic.subject.read
--   school.academic.teaching-assignment.read
--   ai.request.create
--   ai.request.read
--   ai.runtime.execute
--
-- This does not bypass Teacher ownership, tenant isolation, EAIF
-- governance, provider abstraction, or authentication.

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
    'GT-MIGRATION-V269',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V269',
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
    'EAIF',
    definition.description,
    TRUE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V269',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V269',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
CROSS JOIN (
    VALUES
        (
            'ai.request.create',
            'AI Request Create',
            'Create governed enterprise AI requests.'
        ),
        (
            'ai.request.read',
            'AI Request Read',
            'Read governed enterprise AI request state and results.'
        ),
        (
            'ai.runtime.execute',
            'AI Runtime Execute',
            'Execute an authorised governed enterprise AI request.'
        )
) AS definition (
    code,
    name,
    description
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
    'GT-MIGRATION-V269',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V269',
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
