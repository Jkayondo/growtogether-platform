-- ============================================================
-- GT CONNECT — CONVERSATION FULL-TEXT SEARCH
-- ============================================================
--
-- GT-CONNECT-R1-GAP-009
--
-- Release 1 search remains owned by GT Connect because there is
-- currently no generic Enterprise Search capability available.
--
-- No duplicate search table is created.
-- PostgreSQL indexes the authoritative message body directly.
--
-- Search authorization remains enforced by ConnectService before
-- this database query is reached.
-- ============================================================

CREATE INDEX ix_gt_connect_messages_body_search
    ON gt_connect_messages
    USING gin (
        to_tsvector(
            'simple',
            coalesce(body, '')
        )
    )
    WHERE
        deleted_at IS NULL
        AND body IS NOT NULL;
