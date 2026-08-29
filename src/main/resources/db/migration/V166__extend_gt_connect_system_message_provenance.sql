-- ============================================================
-- GT CONNECT — AUTOMATED SYSTEM MESSAGE PROVENANCE
-- ============================================================
--
-- Human-authored messages retain sender_user_id.
--
-- Automated SYSTEM messages may have no human sender, but must
-- identify the originating GT service so that technical/system
-- activity is represented truthfully rather than through a
-- fabricated user account.
-- ============================================================

ALTER TABLE gt_connect_messages
    ALTER COLUMN sender_user_id DROP NOT NULL;

ALTER TABLE gt_connect_messages
    ADD COLUMN source_service varchar(80);

ALTER TABLE gt_connect_messages
    ADD COLUMN source_reference varchar(160);

ALTER TABLE gt_connect_messages
    ADD CONSTRAINT ck_gt_connect_message_sender
        CHECK (
            sender_user_id IS NOT NULL
            OR message_type = 'SYSTEM'
        );

ALTER TABLE gt_connect_messages
    ADD CONSTRAINT ck_gt_connect_automated_system_provenance
        CHECK (
            sender_user_id IS NOT NULL
            OR (
                message_type = 'SYSTEM'
                AND source_service IS NOT NULL
                AND btrim(source_service) <> ''
            )
        );

CREATE INDEX ix_gt_connect_messages_tenant_source
    ON gt_connect_messages (
        tenant_id,
        source_service,
        source_reference
    )
    WHERE source_service IS NOT NULL;
