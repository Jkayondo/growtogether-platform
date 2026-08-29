-- ============================================================
-- GT CONNECT — EXISTING SCHOOL ADMIN MEMBERSHIP BACKFILL
-- ============================================================
--
-- Purpose
-- -------
-- Reconcile ACTIVE EIAM SCHOOL_ADMIN users for canonical
-- SCHOOL_PROFILE institution spaces that existed before the
-- authoritative GT Connect administrator lifecycle was introduced.
--
-- V168 created canonical spaces for historical School Profiles.
-- V172 added privileged-role provenance and normalized qualifying
-- existing ADMIN memberships.
--
-- Neither migration created a missing membership for a user who
-- already held SCHOOL_ADMIN before live role-change reconciliation
-- became available.
--
-- This migration aligns historical data with the current
-- SchoolConnectAdminMembershipProvisioningService semantics:
--
--   ACTIVE SCHOOL_ADMIN
--       ->
--   canonical SCHOOL_PROFILE institution space
--       ->
--   authoritative ACTIVE Connect ADMIN membership
--
-- Existing active ordinary memberships are promoted while their
-- previous Connect role is preserved for later authoritative-role
-- release.
--
-- Memberships governed by a different authority source are not
-- overwritten.
-- ============================================================


-- ------------------------------------------------------------
-- 1. Reconcile an existing ACTIVE membership.
-- ------------------------------------------------------------

UPDATE gt_connect_space_members member
SET
    previous_member_role =
        CASE
            WHEN member.member_role <> 'ADMIN'
                THEN member.member_role
            ELSE member.previous_member_role
        END,
    member_role = 'ADMIN',
    role_authority_source = 'EIAM_SCHOOL_ADMIN',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system',
    version = member.version + 1
FROM gt_connect_spaces space
JOIN gts_school_profile school
  ON school.tenant_id = space.tenant_id
 AND space.space_type = 'INSTITUTION'
 AND space.context_type = 'SCHOOL_PROFILE'
 AND space.context_reference = school.id::text
JOIN eiam_user_role assignment
  ON assignment.tenant_id = space.tenant_id
JOIN eiam_role role
  ON role.id = assignment.role_id
 AND role.tenant_id = assignment.tenant_id
 AND UPPER(role.code) = 'SCHOOL_ADMIN'
JOIN eiam_user_account user_account
  ON user_account.id = assignment.user_id
 AND user_account.tenant_id = assignment.tenant_id
 AND user_account.account_status = 'ACTIVE'
WHERE member.tenant_id = space.tenant_id
  AND member.space_id = space.id
  AND member.user_id = assignment.user_id
  AND member.membership_status = 'ACTIVE'
  AND member.role_authority_source IS NULL;


-- ------------------------------------------------------------
-- 2. Create a missing ACTIVE membership.
-- ------------------------------------------------------------

INSERT INTO gt_connect_space_members (
    id,
    tenant_id,
    space_id,
    user_id,
    member_role,
    joined_at,
    left_at,
    membership_status,
    role_authority_source,
    previous_member_role,
    created_at,
    created_by,
    updated_at,
    updated_by,
    version,
    status
)
SELECT
    gen_random_uuid(),
    space.tenant_id,
    space.id,
    assignment.user_id,
    'ADMIN',
    CURRENT_TIMESTAMP,
    NULL,
    'ACTIVE',
    'EIAM_SCHOOL_ADMIN',
    NULL,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    0,
    'ACTIVE'
FROM gt_connect_spaces space
JOIN gts_school_profile school
  ON school.tenant_id = space.tenant_id
 AND space.space_type = 'INSTITUTION'
 AND space.context_type = 'SCHOOL_PROFILE'
 AND space.context_reference = school.id::text
JOIN eiam_user_role assignment
  ON assignment.tenant_id = space.tenant_id
JOIN eiam_role role
  ON role.id = assignment.role_id
 AND role.tenant_id = assignment.tenant_id
 AND UPPER(role.code) = 'SCHOOL_ADMIN'
JOIN eiam_user_account user_account
  ON user_account.id = assignment.user_id
 AND user_account.tenant_id = assignment.tenant_id
 AND user_account.account_status = 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM gt_connect_space_members member
    WHERE member.tenant_id = space.tenant_id
      AND member.space_id = space.id
      AND member.user_id = assignment.user_id
      AND member.membership_status = 'ACTIVE'
);

