-- Initial GT development tenant bootstrap

INSERT INTO eiam_organization (
    id,
    code,
    name,
    created_at
)
VALUES (
    gen_random_uuid(),
    'GT-AFRICA',
    'GrowTogether Africa',
    CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO NOTHING;


INSERT INTO eiam_tenant (
    id,
    organization_id,
    code,
    name,
    status,
    created_at,
    version
)
SELECT
    gen_random_uuid(),
    id,
    'GT-SCHOOL',
    'GT School',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    0
FROM eiam_organization
WHERE code = 'GT-AFRICA'
ON CONFLICT (code) DO NOTHING;
