-- GrowTogether Enterprise AI Integration Foundation
-- V272 — Safe EAIF configuration and governance foundation.
--
-- Purpose:
--   * Register the five EAIF runtime configuration definitions that the
--     EAIF configuration gateway currently resolves through ECS.
--   * Preserve the existing Java safe fallback values as governed ECS defaults.
--   * Seed DEFAULT_AI_POLICY for tenants that already exist when V272 runs.
--
-- Safe baseline:
--   EAIF_PROVIDER_EXECUTION_ENABLED = false
--   EAIF_HIGH_RISK_APPROVAL_REQUIRED = true
--   EAIF_MAX_INPUT_CHARACTERS = 100000
--   EAIF_REQUEST_RETENTION_DAYS = 90
--   EAIF_DEFAULT_MODEL_CODE = blank
--
-- DEFAULT_AI_POLICY:
--   maximum risk = HIGH
--   approval required = true
--
-- HIGH requests may therefore proceed only through the human approval
-- workflow. CRITICAL remains outside the default policy ceiling.
--
-- Explicit exclusions:
--   * no provider creation
--   * no model creation
--   * no EIP connector creation or modification
--   * no credentials or secrets
--   * no provider execution enablement
--   * no external delivery enablement
--   * no automatic user-role assignment
--
-- Future-tenant DEFAULT_AI_POLICY provisioning is intentionally handled
-- by the controlled tenant-provisioning continuity step following V272.

INSERT INTO ecs_configuration_definitions (
    id,
    code,
    name,
    category,
    description,
    data_type,
    default_value,
    validation_rules,
    allowed_scopes,
    required,
    secret_value,
    active,
    version,
    created_at,
    updated_at
)
VALUES
(
    gen_random_uuid(),
    'EAIF_PROVIDER_EXECUTION_ENABLED',
    'EAIF Provider Execution Enabled',
    'EAIF',
    'Master safety switch controlling whether EAIF may execute requests through an external AI provider.',
    'BOOLEAN',
    'false',
    '{}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    TRUE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'EAIF_HIGH_RISK_APPROVAL_REQUIRED',
    'EAIF High Risk Approval Required',
    'EAIF',
    'Requires governed human approval for high-risk EAIF execution.',
    'BOOLEAN',
    'true',
    '{}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    TRUE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'EAIF_MAX_INPUT_CHARACTERS',
    'EAIF Maximum Input Characters',
    'EAIF',
    'Maximum number of input characters accepted by governed EAIF text execution.',
    'INTEGER',
    '100000',
    '{"minimum":1}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    TRUE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'EAIF_REQUEST_RETENTION_DAYS',
    'EAIF Request Retention Days',
    'EAIF',
    'Default number of days governed EAIF request records are retained.',
    'INTEGER',
    '90',
    '{"minimum":1}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    TRUE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'EAIF_DEFAULT_MODEL_CODE',
    'EAIF Default Model Code',
    'EAIF',
    'Governed default AI model catalogue code. Blank means no default model has been authorised.',
    'STRING',
    '',
    '{}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    FALSE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO NOTHING;

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
    'GT-MIGRATION-V272',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V272',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
WHERE NOT EXISTS (
    SELECT 1
    FROM ai_governance_policies existing
    WHERE existing.tenant_id = tenant.id
      AND existing.policy_code = 'DEFAULT_AI_POLICY'
);
