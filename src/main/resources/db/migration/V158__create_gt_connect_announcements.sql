-- ============================================================
-- GT CONNECT — OFFICIAL ANNOUNCEMENTS
-- ============================================================
--
-- Audience membership remains owned by GT Connect spaces.
-- The announcement is the authoritative publication record.
-- message_id links the publication to its SYSTEM-message
-- projection in the GT Connect conversation.
-- ============================================================

ALTER TABLE gt_connect_messages
    ADD CONSTRAINT uq_gt_connect_messages_id_tenant_space
        UNIQUE (id, tenant_id, space_id);


CREATE TABLE gt_connect_announcements (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,

    space_id uuid NOT NULL,
    message_id uuid NOT NULL,

    published_by_user_id uuid NOT NULL,

    title varchar(200) NOT NULL,
    body text NOT NULL,

    published_at timestamptz NOT NULL,

    created_at timestamptz NOT NULL,
    created_by varchar(150) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(150) NOT NULL,

    version bigint NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT uq_gt_connect_announcement_id_tenant
        UNIQUE (id, tenant_id),

    CONSTRAINT uq_gt_connect_announcement_message
        UNIQUE (tenant_id, message_id),

    CONSTRAINT fk_gt_connect_announcement_message
        FOREIGN KEY (
            message_id,
            tenant_id,
            space_id
        )
        REFERENCES gt_connect_messages (
            id,
            tenant_id,
            space_id
        ),

    CONSTRAINT ck_gt_connect_announcement_title
        CHECK (
            btrim(title) <> ''
        ),

    CONSTRAINT ck_gt_connect_announcement_body
        CHECK (
            btrim(body) <> ''
        )
);


CREATE INDEX ix_gt_connect_announcements_tenant_space_published
    ON gt_connect_announcements (
        tenant_id,
        space_id,
        published_at
    );


CREATE INDEX ix_gt_connect_announcements_tenant_publisher
    ON gt_connect_announcements (
        tenant_id,
        published_by_user_id
    );
