-- GT-CONNECT-001
-- GT-CONNECT-R1-GAP-001C3
-- Release 1 GT Connect permission registration.
--
-- GT Connect is an enterprise shared capability.
-- The current Release 1 implementation is being activated for GT-SCHOOL.
--
-- Existing GT School governance currently establishes SCHOOL_ADMIN
-- as the administrative role. These permissions are assigned there
-- without inventing speculative communication-specific roles.
--
-- Future role refinement may delegate these permissions according to
-- institution policy, safeguarding, relationship and communication context.


-- ============================================================
-- GT CONNECT PERMISSIONS
-- ============================================================

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
        'core.connect.manage',
        'Manage GT Connect',
        'ENTERPRISE_CONNECT',
        'Allows authorised administration of GT Connect spaces, membership and controlled communication structures'
    ),
    (
        'core.connect.moderate',
        'Moderate GT Connect',
        'ENTERPRISE_CONNECT',
        'Allows authorised moderation of GT Connect messages, participants and governed communication activity'
    ),
    (
        'core.announcements.send',
        'Send Official Announcements',
        'ENTERPRISE_CONNECT',
        'Allows authorised publication of official institution announcements through GT Connect'
    )
) AS p(
    code,
    name,
    module,
    description
)
WHERE t.code = 'GT-SCHOOL'
ON CONFLICT DO NOTHING;


-- ============================================================
-- RELEASE 1 ADMINISTRATIVE ASSIGNMENT
-- ============================================================

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
    'core.connect.manage',
    'core.connect.moderate',
    'core.announcements.send'
)
ON CONFLICT DO NOTHING;
