-- ============================================================
-- GT CONNECT — EDS-GOVERNED MESSAGE ATTACHMENTS
-- ============================================================
--
-- GT-CONNECT-R1-GAP-008
--
-- File/content ownership remains in EDS.
-- GT Connect stores only a durable reference to the exact
-- immutable EDS document version that was attached.
--
-- This deliberately prevents:
--   * duplicate file storage in GT Connect;
--   * Connect-owned storage keys;
--   * mutable "latest version" attachment history;
--   * cross-tenant document references.
-- ============================================================

CREATE TABLE gt_connect_message_attachments (

    id uuid PRIMARY KEY,

    tenant_id uuid NOT NULL,

    message_id uuid NOT NULL,

    document_id uuid NOT NULL,

    document_version integer NOT NULL,

    created_at timestamptz NOT NULL,
    created_by varchar(150) NOT NULL,

    updated_at timestamptz NOT NULL,
    updated_by varchar(150) NOT NULL,

    version bigint NOT NULL DEFAULT 0,

    status varchar(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_gt_connect_attachment_message
        FOREIGN KEY (
            message_id,
            tenant_id
        )
        REFERENCES gt_connect_messages (
            id,
            tenant_id
        ),

    /*
     * Tenant-safe and version-specific EDS relationship.
     *
     * V017 already provides:
     *
     * UNIQUE (
     *     tenant_id,
     *     document_id,
     *     version_number
     * )
     */
    CONSTRAINT fk_gt_connect_attachment_document_version
        FOREIGN KEY (
            tenant_id,
            document_id,
            document_version
        )
        REFERENCES eds_document_versions (
            tenant_id,
            document_id,
            version_number
        ),

    CONSTRAINT uq_gt_connect_message_attachment
        UNIQUE (
            tenant_id,
            message_id,
            document_id,
            document_version
        ),

    CONSTRAINT ck_gt_connect_attachment_document_version
        CHECK (
            document_version > 0
        )
);


CREATE INDEX ix_gt_connect_attachment_message
    ON gt_connect_message_attachments (
        tenant_id,
        message_id
    );


CREATE INDEX ix_gt_connect_attachment_document
    ON gt_connect_message_attachments (
        tenant_id,
        document_id,
        document_version
    );
