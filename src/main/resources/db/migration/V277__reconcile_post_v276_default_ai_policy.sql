-- GrowTogether Enterprise AI Integration Foundation
-- V277 — Reconcile post-V276 DEFAULT_AI_POLICY continuity.
--
-- Background:
-- V272 seeded DEFAULT_AI_POLICY for every tenant existing when V272 ran.
-- A tenant provisioned afterwards by an older already-running application
-- instance could therefore exist without DEFAULT_AI_POLICY.
--
-- The runtime provisioning path now creates DEFAULT_AI_POLICY for every
-- newly provisioned tenant. This migration reconciles only any historical
-- gap that remains.
--
-- Governed baseline:
--   policy code       = DEFAULT_AI_POLICY
--   maximum risk      = HIGH
--   approval required = true
--   active            = true
--   status            = ACTIVE
--
-- Explicit exclusions:
--   * no provider creation
--   * no model creation
--   * no connector creation/modification
--   * no credentials
--   * no ECS configuration-value creation
--   * no user-role assignment
--   * no provider-execution enablement
--   * no external-delivery enablement

INSERT INTO ai_governance_policies (
    id,
    tenant_id,
    policy_code,
    policy_name,
    maximum_risk_level,
    approval_required,
    active,
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
    'DEFAULT_AI_POLICY',
    'Default AI Governance Policy',
    'HIGH',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V277',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V277',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM ai_governance_policies existing
    WHERE existing.tenant_id = tenant.id
      AND existing.policy_code = 'DEFAULT_AI_POLICY'
)
ON CONFLICT (tenant_id, policy_code) DO NOTHING;
