-- ============================================================
-- V172 — GT Connect membership role-authority provenance
-- ============================================================
--
-- Allows a Connect membership role to identify an authoritative
-- enterprise source and, where appropriate, preserve the ordinary
-- role that existed before the authoritative role was applied.
--
-- First governed source:
--     EIAM_SCHOOL_ADMIN
--
-- The persistence design remains generic for future GT products.
-- ============================================================

ALTER TABLE gt_connect_space_members
    ADD COLUMN role_authority_source VARCHAR(100),
    ADD COLUMN previous_member_role VARCHAR(30);


ALTER TABLE gt_connect_space_members
    ADD CONSTRAINT ck_gt_connect_role_authority_source
    CHECK (
        role_authority_source IS NULL
        OR btrim(role_authority_source) <> ''
    );


ALTER TABLE gt_connect_space_members
    ADD CONSTRAINT ck_gt_connect_previous_member_role
    CHECK (
        previous_member_role IS NULL
        OR previous_member_role IN (
            'OWNER',
            'ADMIN',
            'MODERATOR',
            'MEMBER'
        )
    );


ALTER TABLE gt_connect_space_members
    ADD CONSTRAINT ck_gt_connect_previous_role_requires_authority
    CHECK (
        previous_member_role IS NULL
        OR role_authority_source IS NOT NULL
    );


CREATE INDEX ix_gt_connect_members_role_authority
    ON gt_connect_space_members (
        tenant_id,
        role_authority_source
    )
    WHERE role_authority_source IS NOT NULL;


-- ============================================================
-- Historical SCHOOL_ADMIN normalization
-- ============================================================
--
-- Canonical SCHOOL_PROFILE privileged membership is now governed
-- by authoritative EIAM School roles.
--
-- Existing ACTIVE ADMIN memberships in canonical School institution
-- spaces are marked EIAM_SCHOOL_ADMIN only when the corresponding
-- user is currently an ACTIVE SCHOOL_ADMIN.
--
-- previous_member_role remains NULL because pre-V172 persistence
-- contains no reliable previous-role evidence.
-- ============================================================

UPDATE gt_connect_space_members member
SET
    role_authority_source = 'EIAM_SCHOOL_ADMIN',
    previous_member_role = NULL,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'migration:V172',
    version = version + 1
WHERE
    member.membership_status = 'ACTIVE'
    AND member.member_role = 'ADMIN'
    AND member.role_authority_source IS NULL

    AND EXISTS (
        SELECT 1

        FROM gt_connect_spaces space

        JOIN gts_school_profile school
          ON school.tenant_id = space.tenant_id
         AND space.context_type = 'SCHOOL_PROFILE'
         AND space.context_reference = school.id::text

        JOIN eiam_user_account user_account
          ON user_account.id = member.user_id
         AND user_account.tenant_id = member.tenant_id

        JOIN eiam_user_role user_role
          ON user_role.user_id = member.user_id
         AND user_role.tenant_id = member.tenant_id

        JOIN eiam_role role
          ON role.id = user_role.role_id
         AND role.tenant_id = member.tenant_id

        WHERE
            space.id = member.space_id
            AND space.tenant_id = member.tenant_id
            AND space.space_type = 'INSTITUTION'

            AND school.status = 'ACTIVE'

            AND user_account.account_status = 'ACTIVE'
            AND user_account.status = 'ACTIVE'

            AND user_role.status = 'ACTIVE'

            AND role.status = 'ACTIVE'
            AND role.code = 'SCHOOL_ADMIN'
    );
