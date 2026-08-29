-- GT-CONNECT-R1-GAP-004B1
-- Per-user GT Connect message delivery/read evidence.
--
-- This table records Connect participant receipt state only.
-- It does not replace ENS notification transport delivery.

CREATE TABLE gt_connect_message_receipts (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    message_id UUID NOT NULL,

    user_id UUID NOT NULL,

    delivered_at TIMESTAMPTZ,

    read_at TIMESTAMPTZ,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(160) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(160) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_gt_connect_receipt_message
        FOREIGN KEY (message_id, tenant_id)
        REFERENCES gt_connect_messages (id, tenant_id),

    CONSTRAINT uq_gt_connect_message_receipt
        UNIQUE (
            tenant_id,
            message_id,
            user_id
        ),

    CONSTRAINT ck_gt_connect_receipt_read_delivery
        CHECK (
            read_at IS NULL
            OR delivered_at IS NOT NULL
        )
);

CREATE INDEX ix_gt_connect_receipts_tenant_message
    ON gt_connect_message_receipts (
        tenant_id,
        message_id
    );

CREATE INDEX ix_gt_connect_receipts_tenant_user
    ON gt_connect_message_receipts (
        tenant_id,
        user_id
    );

CREATE INDEX ix_gt_connect_receipts_user_unread
    ON gt_connect_message_receipts (
        tenant_id,
        user_id,
        read_at
    );
