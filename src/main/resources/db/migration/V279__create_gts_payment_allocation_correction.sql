-- GT School Finance
-- FIN-B5-S3 — Payment Allocation Correction Lifecycle
-- Authority: HQ-GS-FIN-B5-S3-AUTH-001
-- Namespace amendment: HQ-GS-FIN-B5-S3-MIG-AMD-001
--
-- V279 is intentionally used because V278 is occupied by
-- separate, preserved Teacher Programme permission work.
--
-- This migration does not modify V044 or any existing
-- allocation row.

CREATE TABLE gts_payment_allocation_correction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    allocation_id UUID NOT NULL
        REFERENCES gts_payment_allocation(id),

    correction_type VARCHAR(30) NOT NULL,

    reason TEXT NOT NULL,

    replacement_allocation_id UUID
        REFERENCES gts_payment_allocation(id),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT uq_gts_payment_allocation_correction_allocation
        UNIQUE (allocation_id),

    CONSTRAINT ck_gts_payment_allocation_correction_type
        CHECK (
            correction_type IN ('REVERSAL', 'REALLOCATION')
        ),

    CONSTRAINT ck_gts_payment_allocation_correction_reason
        CHECK (
            BTRIM(reason) <> ''
        ),

    CONSTRAINT ck_gts_payment_allocation_correction_replacement
        CHECK (
            (
                correction_type = 'REVERSAL'
                AND replacement_allocation_id IS NULL
            )
            OR
            (
                correction_type = 'REALLOCATION'
                AND replacement_allocation_id IS NOT NULL
            )
        ),

    CONSTRAINT ck_gts_payment_allocation_correction_self_reference
        CHECK (
            replacement_allocation_id IS NULL
            OR replacement_allocation_id <> allocation_id
        )
);

CREATE INDEX ix_gts_payment_allocation_correction_tenant_created
    ON gts_payment_allocation_correction (
        tenant_id,
        created_at
    );

CREATE INDEX ix_gts_payment_allocation_correction_replacement
    ON gts_payment_allocation_correction (
        tenant_id,
        replacement_allocation_id
    )
    WHERE replacement_allocation_id IS NOT NULL;
