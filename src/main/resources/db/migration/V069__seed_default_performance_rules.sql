INSERT INTO gts_performance_rule
(
    id,
    tenant_id,
    rule_name,
    minimum_score,
    maximum_score,
    achievement_status,
    risk_level,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version
)
VALUES

(
    gen_random_uuid(),
    gen_random_uuid(),
    'High Achievement',
    80,
    100,
    'ACHIEVED',
    'LOW',
    NOW(),
    'SYSTEM',
    NOW(),
    'SYSTEM',
    0
),

(
    gen_random_uuid(),
    gen_random_uuid(),
    'Developing Achievement',
    50,
    79.99,
    'DEVELOPING',
    'MEDIUM',
    NOW(),
    'SYSTEM',
    NOW(),
    'SYSTEM',
    0
),

(
    gen_random_uuid(),
    gen_random_uuid(),
    'Needs Support',
    0,
    49.99,
    'NEEDS_SUPPORT',
    'HIGH',
    NOW(),
    'SYSTEM',
    NOW(),
    'SYSTEM',
    0
);
