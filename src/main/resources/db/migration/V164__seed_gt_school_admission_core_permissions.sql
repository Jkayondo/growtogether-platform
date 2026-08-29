-- GT-SCHOOL-A12-HTTP-CORE-001
-- Governed HTTP authorities for the already-existing
-- admission application and admission guardian services.
--
-- These permissions are deliberately separate from:
--   * admission payment authorities;
--   * parent activation authority;
--   * enterprise integration administration.
--
-- They are GT School product permissions and therefore are not
-- added to the generic enterprise TenantProvisioningService.

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
    v.code,
    v.name,
    'SCHOOL_ADMISSION',
    v.description,
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
            'school.admission.application.manage',
            'Manage Admission Applications',
            'Allows authorised creation of tenant-scoped admission applications.'
        ),
        (
            'school.admission.guardian.manage',
            'Manage Admission Guardians',
            'Allows authorised creation of admission-stage guardian records for tenant-scoped applications.'
        )
) AS v(code, name, description)
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
 AND r.status = 'ACTIVE'
JOIN eiam_permission p
  ON p.tenant_id = t.id
 AND p.code IN (
        'school.admission.application.manage',
        'school.admission.guardian.manage'
    )
 AND p.status = 'ACTIVE'
WHERE t.code = 'GT-SCHOOL'
ON CONFLICT DO NOTHING;
