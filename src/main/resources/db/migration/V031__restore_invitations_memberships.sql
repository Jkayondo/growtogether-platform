CREATE TABLE eiam_organization_invitation (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    invitation_status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    invited_by_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_eiam_invitation_token UNIQUE (tenant_id, token_hash),
    CONSTRAINT ck_eiam_invitation_status CHECK (invitation_status IN ('PENDING','ACCEPTED','EXPIRED','REVOKED'))
);

CREATE UNIQUE INDEX uq_eiam_pending_invitation_email
    ON eiam_organization_invitation (tenant_id, lower(email))
    WHERE invitation_status = 'PENDING';
CREATE INDEX ix_eiam_invitation_tenant_status ON eiam_organization_invitation (tenant_id, invitation_status, expires_at);

CREATE TABLE eiam_invitation_role (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    invitation_id UUID NOT NULL REFERENCES eiam_organization_invitation(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES eiam_role(id),
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_eiam_invitation_role UNIQUE (tenant_id, invitation_id, role_id)
);
CREATE INDEX ix_eiam_invitation_role_lookup ON eiam_invitation_role (tenant_id, invitation_id);

CREATE TABLE eiam_tenant_membership (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES eiam_user_account(id),
    membership_status VARCHAR(20) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_eiam_tenant_membership_user UNIQUE (tenant_id, user_id),
    CONSTRAINT ck_eiam_membership_status CHECK (membership_status IN ('ACTIVE','SUSPENDED','REMOVED'))
);
CREATE INDEX ix_eiam_membership_tenant_status ON eiam_tenant_membership (tenant_id, membership_status);
