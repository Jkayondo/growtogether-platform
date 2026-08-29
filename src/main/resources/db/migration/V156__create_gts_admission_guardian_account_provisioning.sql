-- GT School A12.5
-- Admission Guardian -> EIAM Parent Account Provisioning
--
-- This table records admission-stage identity provisioning.
-- It deliberately does NOT create or represent the permanent
-- gts_guardian record; permanent conversion belongs to A12.10.
--
-- Supported provisioning methods:
--
--   SECURE_INVITATION
--       New or existing EIAM identity receives the governed
--       OrganizationInvitation lifecycle.
--
--   EXISTING_IDENTITY_REUSE
--       Reserved for verified multi-child identity reuse where
--       the parent already has the required EIAM identity/access.
--
-- Notification transport (EMAIL/SMS/WHATSAPP/etc.) is NOT stored
-- here. ENS owns transport selection and delivery evidence.


-- ============================================================
-- TENANT-SAFE COMPOSITE REFERENCE FOUNDATIONS
-- ============================================================

ALTER TABLE gts_admission_guardian
    ADD CONSTRAINT uq_gts_admission_guardian_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE eiam_organization_invitation
    ADD CONSTRAINT uq_eiam_invitation_tenant_id
        UNIQUE (tenant_id, id);

ALTER TABLE eiam_user_account
    ADD CONSTRAINT uq_eiam_user_account_tenant_id
        UNIQUE (tenant_id, id);


-- ============================================================
-- ADMISSION GUARDIAN ACCOUNT PROVISIONING
-- ============================================================

CREATE TABLE gts_admission_guardian_account_provisioning (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    admission_guardian_id UUID NOT NULL,

    provisioning_method VARCHAR(40) NOT NULL,

    contact_identity_type VARCHAR(20),
    contact_identity VARCHAR(255),

    invitation_id UUID,

    eiam_user_id UUID,

    provisioning_status VARCHAR(30) NOT NULL
        DEFAULT 'PENDING_ACTIVATION',

    activated_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_gts_adm_gdn_prov_guardian
        FOREIGN KEY (
            tenant_id,
            admission_guardian_id
        )
        REFERENCES gts_admission_guardian (
            tenant_id,
            id
        )
        ON DELETE CASCADE,

    CONSTRAINT fk_gts_adm_gdn_prov_invitation
        FOREIGN KEY (
            tenant_id,
            invitation_id
        )
        REFERENCES eiam_organization_invitation (
            tenant_id,
            id
        )
        ON DELETE RESTRICT,

    CONSTRAINT fk_gts_adm_gdn_prov_user
        FOREIGN KEY (
            tenant_id,
            eiam_user_id
        )
        REFERENCES eiam_user_account (
            tenant_id,
            id
        )
        ON DELETE RESTRICT,

    CONSTRAINT uq_gts_adm_gdn_prov_guardian
        UNIQUE (
            tenant_id,
            admission_guardian_id
        ),

    CONSTRAINT uq_gts_adm_gdn_prov_invitation
        UNIQUE (
            tenant_id,
            invitation_id
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_method
        CHECK (
            provisioning_method IN (
                'SECURE_INVITATION',
                'EXISTING_IDENTITY_REUSE'
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_identity_pair
        CHECK (
            (
                contact_identity_type IS NULL
                AND contact_identity IS NULL
            )
            OR
            (
                contact_identity_type IS NOT NULL
                AND contact_identity IS NOT NULL
                AND btrim(contact_identity) <> ''
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_identity_type
        CHECK (
            contact_identity_type IS NULL
            OR contact_identity_type IN (
                'EMAIL',
                'PHONE'
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_phone
        CHECK (
            contact_identity_type <> 'PHONE'
            OR contact_identity ~ '^\+[1-9][0-9]{5,14}$'
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_method_evidence
        CHECK (
            (
                provisioning_method = 'SECURE_INVITATION'
                AND invitation_id IS NOT NULL
                AND contact_identity_type IS NOT NULL
                AND contact_identity IS NOT NULL
            )
            OR
            (
                provisioning_method = 'EXISTING_IDENTITY_REUSE'
                AND eiam_user_id IS NOT NULL
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_status
        CHECK (
            provisioning_status IN (
                'PENDING_ACTIVATION',
                'ACTIVATED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_pending
        CHECK (
            provisioning_status <> 'PENDING_ACTIVATION'
            OR (
                provisioning_method = 'SECURE_INVITATION'
                AND invitation_id IS NOT NULL
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_activated
        CHECK (
            provisioning_status <> 'ACTIVATED'
            OR (
                eiam_user_id IS NOT NULL
                AND activated_at IS NOT NULL
            )
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_cancelled
        CHECK (
            provisioning_status <> 'CANCELLED'
            OR cancelled_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_adm_gdn_prov_entity_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);

CREATE INDEX ix_gts_adm_gdn_prov_status
    ON gts_admission_guardian_account_provisioning (
        tenant_id,
        provisioning_status
    );

CREATE INDEX ix_gts_adm_gdn_prov_user
    ON gts_admission_guardian_account_provisioning (
        tenant_id,
        eiam_user_id
    )
    WHERE eiam_user_id IS NOT NULL;
