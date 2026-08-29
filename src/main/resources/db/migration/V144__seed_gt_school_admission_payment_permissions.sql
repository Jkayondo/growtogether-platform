-- GT School Release 1
-- A12.4i — Admission Payment Gate security permissions
--
-- IMPROVEMENT:
-- Admission payment authorities are deliberately separated so future
-- Admissions, Bursar, Finance, Waiver Approver and Reconciliation roles
-- can receive only the permissions required for their responsibilities.
--
-- The current repository has SCHOOL_ADMIN as the established GT School
-- administrative role, so all four permissions are initially assigned
-- to SCHOOL_ADMIN without creating speculative new roles.

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
        'school.admission.payment.read',
        'Read Admission Payments',
        'SCHOOL_ADMISSION',
        'Allows viewing admission payment obligations and onboarding gate status'
    ),
    (
        'school.admission.payment.manage',
        'Manage Admission Payments',
        'SCHOOL_ADMISSION',
        'Allows establishment of configured admission obligations and allocation of authoritative payments'
    ),
    (
        'school.admission.payment.waive',
        'Waive Admission Payment',
        'SCHOOL_ADMISSION',
        'Allows authorised human approval of admission payment waivers'
    ),
    (
        'school.admission.payment.reconcile',
        'Reconcile Admission Payments',
        'SCHOOL_ADMISSION',
        'Allows reconciliation of reversed or refunded admission payment evidence'
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
    'school.admission.payment.read',
    'school.admission.payment.manage',
    'school.admission.payment.waive',
    'school.admission.payment.reconcile'
)
ON CONFLICT DO NOTHING;
