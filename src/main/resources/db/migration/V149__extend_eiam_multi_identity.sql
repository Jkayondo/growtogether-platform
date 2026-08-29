-- GT EIAM multi-identity foundation.
--
-- Existing email identities remain valid.
-- New identities may use email or a canonical international phone number.
-- Phone values stored in EIAM must use canonical international form,
-- for example +256701234567.
--
-- Transport choice (SMS / WhatsApp) is intentionally NOT stored here.
-- ENS owns notification transport routing.

ALTER TABLE eiam_user_account
    ALTER COLUMN email DROP NOT NULL,
    ADD COLUMN primary_phone_number VARCHAR(32),
    ADD COLUMN phone_verified_at TIMESTAMPTZ;

ALTER TABLE eiam_user_account
    ADD CONSTRAINT ck_eiam_user_contact_identity
        CHECK (
            (
                email IS NOT NULL
                AND btrim(email) <> ''
            )
            OR
            (
                primary_phone_number IS NOT NULL
                AND btrim(primary_phone_number) <> ''
            )
        ),
    ADD CONSTRAINT ck_eiam_user_primary_phone_e164
        CHECK (
            primary_phone_number IS NULL
            OR primary_phone_number ~ '^\+[1-9][0-9]{5,14}$'
        ),
    ADD CONSTRAINT ck_eiam_user_phone_verification_target
        CHECK (
            phone_verified_at IS NULL
            OR primary_phone_number IS NOT NULL
        );

CREATE UNIQUE INDEX uq_eiam_user_tenant_phone
    ON eiam_user_account (
        tenant_id,
        primary_phone_number
    )
    WHERE primary_phone_number IS NOT NULL;


ALTER TABLE eiam_organization_invitation
    ALTER COLUMN email DROP NOT NULL,
    ADD COLUMN phone_number VARCHAR(32);

ALTER TABLE eiam_organization_invitation
    ADD CONSTRAINT ck_eiam_invitation_contact_identity
        CHECK (
            (
                email IS NOT NULL
                AND btrim(email) <> ''
                AND phone_number IS NULL
            )
            OR
            (
                phone_number IS NOT NULL
                AND btrim(phone_number) <> ''
                AND email IS NULL
            )
        ),
    ADD CONSTRAINT ck_eiam_invitation_phone_e164
        CHECK (
            phone_number IS NULL
            OR phone_number ~ '^\+[1-9][0-9]{5,14}$'
        );

DROP INDEX IF EXISTS uq_eiam_pending_invitation_email;

CREATE UNIQUE INDEX uq_eiam_pending_invitation_email
    ON eiam_organization_invitation (
        tenant_id,
        lower(email)
    )
    WHERE invitation_status = 'PENDING'
      AND email IS NOT NULL;

CREATE UNIQUE INDEX uq_eiam_pending_invitation_phone
    ON eiam_organization_invitation (
        tenant_id,
        phone_number
    )
    WHERE invitation_status = 'PENDING'
      AND phone_number IS NOT NULL;
