-- GT Enterprise Platform
-- V276 — Reconcile complete EAIF AI_ADMIN authorization after V275.
--
-- AI administration is intentionally separated from general school,
-- tenant and integration administration.
--
-- Dedicated specialist role:
--   AI_ADMIN
--
-- Administrative authorities:
--   ai.provider.manage
--   ai.model.manage
--   ai.prompt.manage
--   ai.governance.read
--   ai.audit.read
--   ai.evidence.read
--   ai.request.approval
--
-- AI_ADMIN also receives the existing governed request-read authority:
--   ai.request.read
--
-- Explicit exclusions:
--   * no ai.request.create
--   * no ai.runtime.execute
--   * no integration connector/certification authority
--   * no school authority
--   * no automatic user-role assignment
--   * no provider/model/connector/credential creation
--   * no provider execution or external-delivery enablement

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
    'GT-MIGRATION-V276',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
CROSS JOIN (
    VALUES
        (
            'ai.provider.manage',
            'AI Provider Manage',
            'Manage governed enterprise AI provider registrations.'
        ),
        (
            'ai.model.manage',
            'AI Model Manage',
            'Manage governed enterprise AI model catalogue entries.'
        ),
        (
            'ai.prompt.manage',
            'AI Prompt Manage',
            'Manage governed enterprise AI prompt templates and controls.'
        ),
        (
            'ai.governance.read',
            'AI Governance Read',
            'Read governed enterprise AI governance policy and control state.'
        ),
        (
            'ai.audit.read',
            'AI Audit Read',
            'Read governed enterprise AI audit records.'
        ),
        (
            'ai.evidence.read',
            'AI Evidence Read',
            'Read governed enterprise AI execution evidence.'
        ),
        (
            'ai.request.approval',
            'AI Request Approval',
            'Approve governed enterprise AI requests requiring human authorization.'
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

-- Complete the shared AI request-read permission baseline.
-- V271/V275 intentionally granted this permission to AI_ADMIN when present,
-- but tenants created after the earlier Teacher authorization migrations may
-- not yet have the underlying permission definition.
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
    'ai.request.read',
    'AI Request Read',
    'EAIF',
    'Read governed enterprise AI request state and results.',
    TRUE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_permission existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = 'ai.request.read'
);

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
    'AI_ADMIN',
    'AI Administrator',
    'Specialist administrator for governed enterprise AI administration.',
    TRUE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_role existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = 'AI_ADMIN'
);

WITH ai_admin_permission(code) AS (
    VALUES
        ('ai.provider.manage'),
        ('ai.model.manage'),
        ('ai.prompt.manage'),
        ('ai.governance.read'),
        ('ai.audit.read'),
        ('ai.evidence.read'),
        ('ai.request.approval'),
        ('ai.request.read')
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
    tenant.id,
    role.id,
    permission.id,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V276',
    0,
    'ACTIVE'
FROM ai_admin_permission intended
JOIN eiam_tenant tenant
  ON TRUE
JOIN eiam_role role
  ON role.tenant_id = tenant.id
 AND role.code = 'AI_ADMIN'
 AND role.status = 'ACTIVE'
JOIN eiam_permission permission
  ON permission.tenant_id = tenant.id
 AND permission.code = intended.code
 AND permission.status = 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_role_permission existing
    WHERE existing.tenant_id = tenant.id
      AND existing.role_id = role.id
      AND existing.permission_id = permission.id
);
