-- GT-SCHOOL-A12.5-B8-HTTP-001
-- Secure parent-account activation notification authority.
--
-- The activation workflow:
--   * creates/reuses the governed EIAM identity;
--   * creates the admission-stage provisioning evidence;
--   * creates the ordinary ENS notification without the secret token;
--   * attaches the encrypted transient V159 secure provider payload.
--
-- This permission is deliberately separate from guardian CRUD and
-- admission-payment authorities because activation causes external
-- identity/notification side effects.

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
    'school.admission.parent-activation.manage',
    'Manage Admission Parent Activation',
    'SCHOOL_ADMISSION',
    'Allows authorised creation and secure delivery of admission-stage parent account activation invitations.',
    false,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
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
 AND p.code = 'school.admission.parent-activation.manage'
 AND p.status = 'ACTIVE'
WHERE t.code = 'GT-SCHOOL'
ON CONFLICT DO NOTHING;
