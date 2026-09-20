-- GT School Finance
-- FIN-B5-S4 — Student Payment Receipt & Acknowledgement Lifecycle
--
-- Purpose:
-- tenant-wide atomic sequence component for durable receipt numbers.
--
-- Receipt formatting is resolved through Enterprise Configuration (ECS).
-- This migration does not recreate or alter gts_payment_receipt.
-- This migration does not add IAM permissions.
-- This migration does not introduce provider-specific behaviour.

CREATE TABLE gts_payment_receipt_number_sequence (
    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    last_issued_number BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_gts_payment_receipt_number_sequence
        PRIMARY KEY (tenant_id),

    CONSTRAINT ck_gts_payment_receipt_number_sequence_positive
        CHECK (last_issued_number >= 0)
);

-- ============================================================
-- ECS RECEIPT NUMBER FORMAT DEFINITION
-- ============================================================
--
-- The receipt number itself remains a durable financial identity
-- stored once in gts_payment_receipt.
--
-- ECS controls only the presentation format used when that identity
-- is first generated.
--
-- PLATFORM provides the generic baseline.
-- TENANT permits an authorised institution-specific override.
--
-- Supported runtime format grammar is enforced by
-- FinancePaymentReceiptNumberService:
--
--   {sequence}
--   {sequence:N}
--
-- where N is between 1 and 18.
--
-- Exactly one sequence token is required and the final rendered
-- receipt number must be between 1 and 100 characters.
--
-- ECS currently provides type validation for STRING values; therefore
-- format grammar validation intentionally remains in the receipt
-- number service rather than being represented as an unenforced ECS
-- validation_rules contract.

INSERT INTO ecs_configuration_definitions (
    id,
    code,
    name,
    category,
    description,
    data_type,
    default_value,
    validation_rules,
    allowed_scopes,
    required,
    secret_value,
    active,
    version,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'GT_SCHOOL_FINANCE_RECEIPT_NUMBER_FORMAT',
    'GT School Finance Receipt Number Format',
    'SCHOOL_FINANCE',
    'Controls the presentation format used when a durable GT School payment receipt number is first generated. The format must contain exactly one {sequence} or {sequence:N} token; N must be between 1 and 18 and the final rendered number must not exceed 100 characters.',
    'STRING',
    'RCT-{sequence:10}',
    '{}'::jsonb,
    ARRAY['PLATFORM', 'TENANT'],
    TRUE,
    FALSE,
    TRUE,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO NOTHING;
