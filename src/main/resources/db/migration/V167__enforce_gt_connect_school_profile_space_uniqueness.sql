-- ============================================================
-- GT CONNECT — CANONICAL SCHOOL PROFILE SPACE
-- ============================================================
--
-- A School Profile may map to only one canonical GT Connect
-- INSTITUTION space.
--
-- The uniqueness includes the profile reference rather than
-- tenant alone so the model remains compatible with future
-- multi-institution tenants.
-- ============================================================

CREATE UNIQUE INDEX
    ux_gt_connect_school_profile_institution_space
ON gt_connect_spaces (
    tenant_id,
    context_type,
    context_reference
)
WHERE
    space_type = 'INSTITUTION'
    AND context_type = 'SCHOOL_PROFILE'
    AND context_reference IS NOT NULL;
