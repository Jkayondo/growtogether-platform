-- GT-EIAM-R1-FIX-002
-- Enterprise Integration Platform authorization foundation.
--
-- Establishes the missing EIP permission definitions and a dedicated
-- INTEGRATION_ADMIN role without expanding SCHOOL_ADMIN into an
-- enterprise infrastructure or financial-operations administrator.
--
-- Financial execution authorities are deliberately defined but are NOT
-- assigned to INTEGRATION_ADMIN. They require future finance/payment
-- governance and role assignment.
--
-- Existing GT-SCHOOL recovery rule:
-- If exactly one ACTIVE user currently holds ACTIVE SCHOOL_ADMIN,
-- that user receives INTEGRATION_ADMIN so the governed EIP administration
-- boundary is usable without creating a security bypass.

-- ============================================================
-- EIP PERMISSION DEFINITIONS
-- ============================================================

WITH eip_permission(code, name, description) AS (
    VALUES
        ('integration.admin.read',
         'Read Integration Administration',
         'Allows viewing enterprise integration administration information.'),

        ('integration.analytics.read',
         'Read Integration Analytics',
         'Allows viewing enterprise integration analytics.'),

        ('integration.certification.manage',
         'Manage Integration Certifications',
         'Allows creating and certifying external connector certifications.'),

        ('integration.certification.read',
         'Read Integration Certifications',
         'Allows viewing external connector certifications.'),

        ('integration.connector.manage',
         'Manage Integration Connectors',
         'Allows creating and managing external integration connectors.'),

        ('integration.connector.read',
         'Read Integration Connectors',
         'Allows viewing external integration connectors.'),

        ('integration.dispute.manage',
         'Manage Integration Disputes',
         'Allows management of payment-provider disputes.'),

        ('integration.event.publish',
         'Publish Integration Events',
         'Allows publishing enterprise integration runtime events.'),

        ('integration.event.read',
         'Read Integration Events',
         'Allows viewing enterprise integration runtime events.'),

        ('integration.gateway.manage',
         'Manage Integration Gateway',
         'Allows management of enterprise integration gateway routes.'),

        ('integration.gateway.read',
         'Read Integration Gateway',
         'Allows viewing enterprise integration gateway routes.'),

        ('integration.payment.create',
         'Create Integration Payments',
         'Allows creation of provider-neutral payment transactions.'),

        ('integration.payment.execute',
         'Execute Integration Payments',
         'Allows provider execution lifecycle actions for payments.'),

        ('integration.payment.manage',
         'Manage Integration Payments',
         'Allows management of provider-neutral payment transactions.'),

        ('integration.payment.read',
         'Read Integration Payments',
         'Allows viewing provider-neutral payment transactions.'),

        ('integration.payment.reverse',
         'Reverse Integration Payments',
         'Allows reversal of eligible completed payment transactions.'),

        ('integration.reconciliation.manage',
         'Manage Integration Reconciliation',
         'Allows management of payment reconciliation operations.'),

        ('integration.replay',
         'Replay Integration Messages',
         'Allows governed replay of integration runtime messages.'),

        ('integration.route.manage',
         'Manage Integration Routes',
         'Allows management of integration runtime routing.'),

        ('integration.runtime.manage',
         'Manage Integration Runtime',
         'Allows management of integration runtime message lifecycle.'),

        ('integration.settlement.manage',
         'Manage Integration Settlements',
         'Allows management of payment settlement operations.'),

        ('integration.transformation.manage',
         'Manage Integration Transformations',
         'Allows management of integration transformation rules.'),

        ('integration.transformation.read',
         'Read Integration Transformations',
         'Allows viewing integration transformation rules.'),

        ('integration.webhook.manage',
         'Manage Integration Webhooks',
         'Allows management of enterprise integration webhooks.'),

        ('integration.webhook.read',
         'Read Integration Webhooks',
         'Allows viewing enterprise integration webhooks.')
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
    ep.code,
    ep.name,
    'INTEGRATION',
    ep.description,
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
CROSS JOIN eip_permission ep
ON CONFLICT DO NOTHING;


-- ============================================================
-- DEDICATED INTEGRATION ADMINISTRATOR ROLE
-- ============================================================

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
    t.id,
    'INTEGRATION_ADMIN',
    'Integration Administrator',
    'Specialist administrator for governed enterprise integration infrastructure.',
    TRUE,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM eiam_tenant t
ON CONFLICT DO NOTHING;


-- ============================================================
-- INTEGRATION ADMINISTRATOR ALLOW-LIST
-- ============================================================
--
-- Deliberately excludes:
-- integration.payment.create
-- integration.payment.execute
-- integration.payment.manage
-- integration.payment.read
-- integration.payment.reverse
-- integration.settlement.manage
-- integration.reconciliation.manage
-- integration.dispute.manage

WITH integration_admin_permission(code) AS (
    VALUES
        ('integration.admin.read'),
        ('integration.analytics.read'),
        ('integration.certification.manage'),
        ('integration.certification.read'),
        ('integration.connector.manage'),
        ('integration.connector.read'),
        ('integration.event.publish'),
        ('integration.event.read'),
        ('integration.gateway.manage'),
        ('integration.gateway.read'),
        ('integration.replay'),
        ('integration.route.manage'),
        ('integration.runtime.manage'),
        ('integration.transformation.manage'),
        ('integration.transformation.read'),
        ('integration.webhook.manage'),
        ('integration.webhook.read')
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
FROM integration_admin_permission intended
JOIN eiam_tenant t
    ON TRUE
JOIN eiam_role r
    ON r.tenant_id = t.id
   AND r.code = 'INTEGRATION_ADMIN'
JOIN eiam_permission p
    ON p.tenant_id = t.id
   AND p.code = intended.code
ON CONFLICT DO NOTHING;


-- ============================================================
-- GT-SCHOOL LEGACY RECOVERY ASSIGNMENT
-- ============================================================
--
-- Assign only when there is exactly one distinct ACTIVE SCHOOL_ADMIN.
-- If there are zero or multiple candidates, assign nobody.

WITH candidate AS (
    SELECT DISTINCT
        u.tenant_id,
        u.id AS user_id
    FROM eiam_user_account u
    JOIN eiam_tenant t
      ON t.id = u.tenant_id
     AND t.code = 'GT-SCHOOL'
    JOIN eiam_user_role ur
      ON ur.tenant_id = u.tenant_id
     AND ur.user_id = u.id
     AND ur.status = 'ACTIVE'
    JOIN eiam_role r
      ON r.tenant_id = ur.tenant_id
     AND r.id = ur.role_id
     AND r.code = 'SCHOOL_ADMIN'
     AND r.status = 'ACTIVE'
    WHERE u.account_status = 'ACTIVE'
),
sole_candidate AS (
    SELECT
        c.tenant_id,
        c.user_id
    FROM candidate c
    WHERE (
        SELECT COUNT(*)
        FROM candidate x
        WHERE x.tenant_id = c.tenant_id
    ) = 1
)
INSERT INTO eiam_user_role (
    id,
    tenant_id,
    user_id,
    role_id,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    sc.tenant_id,
    sc.user_id,
    ir.id,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM sole_candidate sc
JOIN eiam_role ir
  ON ir.tenant_id = sc.tenant_id
 AND ir.code = 'INTEGRATION_ADMIN'
ON CONFLICT DO NOTHING;
