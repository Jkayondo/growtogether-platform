-- GT Enterprise Platform
-- V282 — Define GT School Leadership Overview permission.
--
-- Authority:
--   HQ-GS-PILOT-LEADERSHIP-001
--
-- Permission:
--
--   school.leadership.overview.read
--
-- Purpose:
--   Establishes the dedicated least-privilege authority required to read the
--   GT School Leadership overview.
--
-- Security boundaries:
--   * this migration defines the permission for existing tenants only;
--   * NO existing role receives the permission in this migration;
--   * the permission does not imply learner, finance, teacher, assessment,
--     attendance, workflow, approval or mutation authority;
--   * sensitive drill-down remains governed by the relevant underlying-domain
--     permissions;
--   * tenant isolation remains server-enforced;
--   * Leadership remains a read-only aggregation and decision-support surface.
--
-- New-tenant provisioning:
--   TenantProvisioningService must carry the equivalent permission definition
--   before this candidate is committed. That conformity step is intentionally
--   separate so no role assignment is inferred or invented.

WITH permission_seed (
    code,
    name,
    description
) AS (
    VALUES
        (
            'school.leadership.overview.read',
            'Read School Leadership Overview',
            'Allows an authorised school leadership user to read the tenant-scoped GT School Leadership overview and its permitted aggregate indicators.'
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
    tenant.id,
    seed.code,
    seed.name,
    'SCHOOL_LEADERSHIP',
    seed.description,
    FALSE,
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V282',
    CURRENT_TIMESTAMP,
    'GT-MIGRATION-V282',
    0,
    'ACTIVE'
FROM eiam_tenant tenant
CROSS JOIN permission_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM eiam_permission existing
    WHERE existing.tenant_id = tenant.id
      AND existing.code = seed.code
);
