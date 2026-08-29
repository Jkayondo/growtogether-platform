-- GT-ENS-R1-FIX-001
-- Enterprise Notification Service authorization and provider-route
-- administration foundation.
--
-- Existing ENS HTTP endpoints already reference:
--   notification.send
--   notification.read
--   notification.queue.manage
--
-- but those authorities were not present in EIAM.
--
-- This migration also introduces narrowly scoped provider-routing
-- administration authorities:
--   notification.route.read
--   notification.route.manage
--
-- Governance:
--   * INTEGRATION_ADMIN receives ONLY route read/manage.
--   * notification.send/read/queue.manage remain defined but unassigned.
--   * SCHOOL_ADMIN receives no notification infrastructure authority.
--   * This migration does not configure any provider route and does not
--     enable external provider delivery.


-- ============================================================
-- ENS PERMISSION DEFINITIONS
-- ============================================================

WITH notification_permission(code, name, description) AS (
    VALUES
        (
            'notification.send',
            'Send Notifications',
            'Allows submission of enterprise notification requests.'
        ),
        (
            'notification.read',
            'Read Notifications',
            'Allows viewing enterprise notification requests.'
        ),
        (
            'notification.queue.manage',
            'Manage Notification Queue',
            'Allows governed management of notification queue lifecycle.'
        ),
        (
            'notification.route.read',
            'Read Notification Provider Routes',
            'Allows viewing tenant notification provider routing configuration.'
        ),
        (
            'notification.route.manage',
            'Manage Notification Provider Routes',
            'Allows governed creation and management of tenant notification provider routes.'
        )
)
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
    np.code,
    np.name,
    'NOTIFICATION',
    np.description,
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
CROSS JOIN notification_permission np
ON CONFLICT DO NOTHING;


-- ============================================================
-- INTEGRATION ADMINISTRATOR ROUTE ALLOW-LIST
-- ============================================================

WITH integration_notification_permission(code) AS (
    VALUES
        ('notification.route.read'),
        ('notification.route.manage')
)
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
FROM integration_notification_permission intended
JOIN eiam_tenant t
    ON TRUE
JOIN eiam_role r
    ON r.tenant_id = t.id
   AND r.code = 'INTEGRATION_ADMIN'
   AND r.status = 'ACTIVE'
JOIN eiam_permission p
    ON p.tenant_id = t.id
   AND p.code = intended.code
   AND p.status = 'ACTIVE'
ON CONFLICT DO NOTHING;
