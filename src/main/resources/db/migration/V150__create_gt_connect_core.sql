-- GT-CONNECT-001
-- GT-CONNECT-R1-GAP-001A
-- Core GT Connect persistence foundation.
--
-- Scope:
--   * conversation/space
--   * authorised membership record
--   * message
--
-- Explicitly NOT included here:
--   * delivery/read receipts
--   * reactions
--   * mentions
--   * announcements
--   * notification routing
--   * voice/video/meeting
--   * AI collaboration
--
-- GT Connect consumes Enterprise IAM, ENS, Workflow, Events,
-- Documents/Files, Audit and Configuration rather than duplicating them.


-- ============================================================
-- GT CONNECT SPACES
-- ============================================================

CREATE TABLE gt_connect_spaces (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,

    space_type varchar(30) NOT NULL,
    name varchar(200),

    context_type varchar(60),
    context_reference varchar(160),

    created_at timestamptz NOT NULL,
    created_by varchar(150) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(150) NOT NULL,

    version bigint NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT ck_gt_connect_space_type
        CHECK (
            space_type IN (
                'DIRECT',
                'GROUP',
                'CLASS',
                'DEPARTMENT',
                'INSTITUTION',
                'SYSTEM'
            )
        ),

    CONSTRAINT uq_gt_connect_spaces_id_tenant
        UNIQUE (id, tenant_id)
);

CREATE INDEX ix_gt_connect_spaces_tenant_type
    ON gt_connect_spaces (tenant_id, space_type);

CREATE INDEX ix_gt_connect_spaces_tenant_context
    ON gt_connect_spaces (
        tenant_id,
        context_type,
        context_reference
    );


-- ============================================================
-- GT CONNECT SPACE MEMBERS
-- ============================================================

CREATE TABLE gt_connect_space_members (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,

    space_id uuid NOT NULL,
    user_id uuid NOT NULL,

    member_role varchar(30) NOT NULL DEFAULT 'MEMBER',

    joined_at timestamptz NOT NULL,
    left_at timestamptz,

    membership_status varchar(20) NOT NULL DEFAULT 'ACTIVE',

    created_at timestamptz NOT NULL,
    created_by varchar(150) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(150) NOT NULL,

    version bigint NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_gt_connect_member_space
        FOREIGN KEY (space_id, tenant_id)
        REFERENCES gt_connect_spaces (id, tenant_id),

    CONSTRAINT ck_gt_connect_member_role
        CHECK (
            member_role IN (
                'OWNER',
                'ADMIN',
                'MODERATOR',
                'MEMBER'
            )
        ),

    CONSTRAINT ck_gt_connect_membership_status
        CHECK (
            membership_status IN (
                'ACTIVE',
                'LEFT',
                'REMOVED'
            )
        )
);

CREATE INDEX ix_gt_connect_members_tenant_space
    ON gt_connect_space_members (tenant_id, space_id);

CREATE INDEX ix_gt_connect_members_tenant_user
    ON gt_connect_space_members (tenant_id, user_id);

CREATE UNIQUE INDEX uq_gt_connect_active_member
    ON gt_connect_space_members (
        tenant_id,
        space_id,
        user_id
    )
    WHERE membership_status = 'ACTIVE';


-- ============================================================
-- GT CONNECT MESSAGES
-- ============================================================

CREATE TABLE gt_connect_messages (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,

    space_id uuid NOT NULL,
    sender_user_id uuid NOT NULL,

    message_type varchar(30) NOT NULL,
    body text,

    reply_to_message_id uuid,

    sent_at timestamptz NOT NULL,
    edited_at timestamptz,
    deleted_at timestamptz,

    created_at timestamptz NOT NULL,
    created_by varchar(150) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(150) NOT NULL,

    version bigint NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT uq_gt_connect_messages_id_tenant
        UNIQUE (id, tenant_id),

    CONSTRAINT fk_gt_connect_message_space
        FOREIGN KEY (space_id, tenant_id)
        REFERENCES gt_connect_spaces (id, tenant_id),

    CONSTRAINT fk_gt_connect_message_reply
        FOREIGN KEY (reply_to_message_id, tenant_id)
        REFERENCES gt_connect_messages (id, tenant_id),

    CONSTRAINT ck_gt_connect_message_type
        CHECK (
            message_type IN (
                'TEXT',
                'IMAGE',
                'FILE',
                'AUDIO',
                'VIDEO',
                'LOCATION',
                'POLL',
                'SYSTEM'
            )
        ),

    CONSTRAINT ck_gt_connect_text_message_body
        CHECK (
            message_type <> 'TEXT'
            OR (
                body IS NOT NULL
                AND btrim(body) <> ''
            )
        )
);

CREATE INDEX ix_gt_connect_messages_tenant_space_sent
    ON gt_connect_messages (
        tenant_id,
        space_id,
        sent_at
    );

CREATE INDEX ix_gt_connect_messages_tenant_sender
    ON gt_connect_messages (
        tenant_id,
        sender_user_id
    );

CREATE INDEX ix_gt_connect_messages_reply
    ON gt_connect_messages (
        tenant_id,
        reply_to_message_id
    )
    WHERE reply_to_message_id IS NOT NULL;
