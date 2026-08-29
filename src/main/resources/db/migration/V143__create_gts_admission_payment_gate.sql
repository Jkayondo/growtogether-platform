-- GT School Admission Payment Gate
--
-- Admission applicants do not yet have permanent gts_student or
-- gts_student_financial_account records. This bridge allows admission-stage
-- obligations to reuse GT fee definitions and the Enterprise Integration
-- Platform payment engine without creating fake Student records.

CREATE TABLE gts_admission_payment_obligation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    admission_application_id UUID NOT NULL
        REFERENCES gts_admission_application(id) ON DELETE CASCADE,

    fee_item_id UUID NOT NULL
        REFERENCES gts_fee_item(id),

    currency_code VARCHAR(3) NOT NULL,

    required_amount NUMERIC(18,2) NOT NULL,
    waived_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    required_for_onboarding BOOLEAN NOT NULL DEFAULT TRUE,

    gate_status VARCHAR(30) NOT NULL DEFAULT 'PAYMENT_REQUIRED',

    waiver_reason VARCHAR(1000),
    waived_at TIMESTAMPTZ,
    waived_by UUID,

    satisfied_at TIMESTAMPTZ,
    satisfied_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_admission_payment_obligation
        UNIQUE (
            tenant_id,
            admission_application_id,
            fee_item_id
        ),

    CONSTRAINT ck_gts_admission_payment_obligation_amounts
        CHECK (
            required_amount >= 0
            AND waived_amount >= 0
            AND waived_amount <= required_amount
        ),

    CONSTRAINT ck_gts_admission_payment_obligation_gate_status
        CHECK (
            gate_status IN (
                'PAYMENT_REQUIRED',
                'PAYMENT_PENDING',
                'PARTIALLY_SATISFIED',
                'SATISFIED',
                'WAIVED',
                'REVIEW_REQUIRED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_admission_payment_obligation_waiver
        CHECK (
            waived_amount = 0
            OR (
                waived_at IS NOT NULL
                AND waived_by IS NOT NULL
                AND waiver_reason IS NOT NULL
            )
        ),

    CONSTRAINT ck_gts_admission_payment_obligation_satisfaction
        CHECK (
            gate_status NOT IN ('SATISFIED', 'WAIVED')
            OR satisfied_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_admission_payment_obligation_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);

CREATE INDEX ix_gts_admission_payment_obligation_application
    ON gts_admission_payment_obligation (
        tenant_id,
        admission_application_id,
        gate_status
    );


CREATE TABLE gts_admission_payment_allocation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    admission_payment_obligation_id UUID NOT NULL
        REFERENCES gts_admission_payment_obligation(id)
        ON DELETE CASCADE,

    eip_payment_transaction_id UUID NOT NULL
        REFERENCES eip_payment_transactions(id),

    allocated_amount NUMERIC(18,2) NOT NULL,

    allocation_status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',

    applied_at TIMESTAMPTZ NOT NULL,
    applied_by UUID,

    reversed_at TIMESTAMPTZ,
    reversed_by UUID,
    reversal_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_admission_payment_allocation
        UNIQUE (
            tenant_id,
            admission_payment_obligation_id,
            eip_payment_transaction_id
        ),

    CONSTRAINT ck_gts_admission_payment_allocation_amount
        CHECK (allocated_amount > 0),

    CONSTRAINT ck_gts_admission_payment_allocation_lifecycle
        CHECK (
            allocation_status IN (
                'APPLIED',
                'REVERSED',
                'REFUNDED'
            )
        ),

    CONSTRAINT ck_gts_admission_payment_allocation_reversal
        CHECK (
            allocation_status <> 'REVERSED'
            OR reversed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_admission_payment_allocation_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);

CREATE INDEX ix_gts_admission_payment_allocation_obligation
    ON gts_admission_payment_allocation (
        tenant_id,
        admission_payment_obligation_id,
        allocation_status
    );

CREATE INDEX ix_gts_admission_payment_allocation_eip
    ON gts_admission_payment_allocation (
        tenant_id,
        eip_payment_transaction_id
    );


CREATE TABLE gts_admission_payment_gate_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    admission_payment_obligation_id UUID NOT NULL
        REFERENCES gts_admission_payment_obligation(id)
        ON DELETE CASCADE,

    previous_gate_status VARCHAR(30),
    new_gate_status VARCHAR(30) NOT NULL,

    reason VARCHAR(1000),

    changed_at TIMESTAMPTZ NOT NULL,
    changed_by UUID,

    correlation_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_admission_payment_gate_history_previous
        CHECK (
            previous_gate_status IS NULL
            OR previous_gate_status IN (
                'PAYMENT_REQUIRED',
                'PAYMENT_PENDING',
                'PARTIALLY_SATISFIED',
                'SATISFIED',
                'WAIVED',
                'REVIEW_REQUIRED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_admission_payment_gate_history_new
        CHECK (
            new_gate_status IN (
                'PAYMENT_REQUIRED',
                'PAYMENT_PENDING',
                'PARTIALLY_SATISFIED',
                'SATISFIED',
                'WAIVED',
                'REVIEW_REQUIRED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_admission_payment_gate_history_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'ARCHIVED'
            )
        )
);

CREATE INDEX ix_gts_admission_payment_gate_history_obligation
    ON gts_admission_payment_gate_history (
        tenant_id,
        admission_payment_obligation_id,
        changed_at
    );
