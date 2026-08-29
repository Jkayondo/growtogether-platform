-- GrowTogether Enterprise Integration Platform
-- EIP governed runtime execution configuration.
--
-- IMPROVEMENT:
-- Registers EIP settings that were previously referenced through ECS
-- but depended only on Java fallback values.
--
-- PLATFORM provides the safe enterprise baseline.
-- TENANT permits an institution-specific override where authorised.

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
    'EIP_MAX_ATTEMPTS',
    'EIP Maximum Attempts',
    'EIP',
    'Maximum number of external integration execution attempts.',
    'INTEGER',
    '5',
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
    'EIP_REQUEST_TIMEOUT_SECONDS',
    'EIP Request Timeout Seconds',
    'EIP',
    'Maximum time in seconds allowed for an external integration request.',
    'INTEGER',
    '30',
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
    'EIP_CIRCUIT_FAILURE_THRESHOLD',
    'EIP Circuit Failure Threshold',
    'EIP',
    'Number of provider failures before circuit protection should intervene.',
    'INTEGER',
    '5',
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
    'EIP_EXTERNAL_DELIVERY_ENABLED',
    'EIP External Delivery Enabled',
    'EIP',
    'Master safety switch controlling whether external provider delivery is permitted.',
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
    'EIP_EXECUTION_ENVIRONMENT',
    'EIP Execution Environment',
    'EIP',
    'Runtime environment used when validating connector certification before external execution.',
    'ENUM',
    'DEVELOPMENT',
    '{"allowedValues":["DEVELOPMENT","TEST","STAGING","PRODUCTION"]}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    TRUE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO NOTHING;
