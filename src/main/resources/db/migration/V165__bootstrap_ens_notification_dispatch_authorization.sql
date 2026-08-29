-- GT-ENS-R1-DISPATCH-001
-- Governed ENS external-provider dispatch boundary.
--
-- Governance:
--   * introduces notification.dispatch.manage;
--   * assigns it only to active INTEGRATION_ADMIN roles;
--   * does NOT grant notification.queue.manage;
--   * does NOT grant SCHOOL_ADMIN notification infrastructure authority;
--   * does NOT enable external provider delivery;
--   * does NOT dispatch any notification.

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
    'notification.dispatch.manage',
    'Manage Notification Dispatch',
    'NOTIFICATION',
    'Allows authorised execution of queued notifications through governed ENS provider routing.',
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
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
 AND r.code = 'INTEGRATION_ADMIN'
 AND r.status = 'ACTIVE'
JOIN eiam_permission p
  ON p.tenant_id = t.id
 AND p.code = 'notification.dispatch.manage'
 AND p.status = 'ACTIVE'
ON CONFLICT DO NOTHING;
