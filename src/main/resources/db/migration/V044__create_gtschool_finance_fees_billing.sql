CREATE TABLE gts_fee_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    category_code VARCHAR(80) NOT NULL,
    category_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    category_type VARCHAR(40) NOT NULL,
    accounting_code VARCHAR(100),

    refundable BOOLEAN NOT NULL DEFAULT FALSE,
    mandatory_by_default BOOLEAN NOT NULL DEFAULT TRUE,
    recurring BOOLEAN NOT NULL DEFAULT TRUE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_fee_category_code
        UNIQUE (tenant_id, category_code),

    CONSTRAINT ck_gts_fee_category_type
        CHECK (
            category_type IN (
                'TUITION',
                'ADMISSION',
                'REGISTRATION',
                'BOARDING',
                'TRANSPORT',
                'MEALS',
                'EXAMINATION',
                'LIBRARY',
                'LABORATORY',
                'ICT',
                'UNIFORM',
                'ACTIVITY',
                'MEDICAL',
                'DEVELOPMENT',
                'SECURITY',
                'GRADUATION',
                'PENALTY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_fee_category_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_fee_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    fee_category_id UUID NOT NULL
        REFERENCES gts_fee_category(id),

    item_code VARCHAR(100) NOT NULL,
    item_name VARCHAR(250) NOT NULL,
    description VARCHAR(1200),

    currency_code VARCHAR(3) NOT NULL,
    default_amount NUMERIC(18,2),

    charge_frequency VARCHAR(30) NOT NULL DEFAULT 'TERM',
    quantity_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    partial_payment_allowed BOOLEAN NOT NULL DEFAULT TRUE,

    tax_applicable BOOLEAN NOT NULL DEFAULT FALSE,
    tax_code VARCHAR(80),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_fee_item_code
        UNIQUE (tenant_id, item_code),

    CONSTRAINT ck_gts_fee_item_amount
        CHECK (
            default_amount IS NULL
            OR default_amount >= 0
        ),

    CONSTRAINT ck_gts_fee_item_frequency
        CHECK (
            charge_frequency IN (
                'ONCE',
                'DAILY',
                'WEEKLY',
                'MONTHLY',
                'TERM',
                'SEMESTER',
                'ACADEMIC_YEAR',
                'PER_USE',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_fee_item_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_fee_structure (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    structure_code VARCHAR(100) NOT NULL,
    structure_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID
        REFERENCES gts_campus(id),

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    study_track_id UUID
        REFERENCES gts_study_track(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    currency_code VARCHAR(3) NOT NULL,

    effective_from DATE NOT NULL,
    effective_to DATE,

    workflow_instance_id UUID,
    approved_at TIMESTAMPTZ,
    approved_by UUID,

    structure_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_fee_structure_code
        UNIQUE (tenant_id, structure_code),

    CONSTRAINT uq_gts_fee_structure_scope
        UNIQUE (
            tenant_id,
            academic_year_id,
            academic_term_id,
            campus_id,
            academic_programme_id,
            study_track_id,
            class_grade_id,
            stream_id
        ),

    CONSTRAINT ck_gts_fee_structure_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_fee_structure_approval
        CHECK (
            structure_status NOT IN ('APPROVED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_fee_structure_lifecycle
        CHECK (
            structure_status IN (
                'DRAFT',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'SUSPENDED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_fee_structure_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_fee_structure_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    fee_structure_id UUID NOT NULL
        REFERENCES gts_fee_structure(id) ON DELETE CASCADE,

    fee_item_id UUID NOT NULL
        REFERENCES gts_fee_item(id),

    amount NUMERIC(18,2) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL DEFAULT 1,

    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    refundable BOOLEAN NOT NULL DEFAULT FALSE,

    due_date DATE,
    sequence_number INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_fee_structure_item
        UNIQUE (tenant_id, fee_structure_id, fee_item_id),

    CONSTRAINT uq_gts_fee_structure_item_sequence
        UNIQUE (tenant_id, fee_structure_id, sequence_number),

    CONSTRAINT ck_gts_fee_structure_item_values
        CHECK (
            amount >= 0
            AND quantity > 0
            AND sequence_number > 0
        ),

    CONSTRAINT ck_gts_fee_structure_item_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_financial_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    account_number VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    currency_code VARCHAR(3) NOT NULL,

    opening_balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    current_balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    credit_balance NUMERIC(18,2) NOT NULL DEFAULT 0,

    credit_limit NUMERIC(18,2),
    billing_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    opened_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,
    closed_by UUID,
    closure_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_financial_account_number
        UNIQUE (tenant_id, account_number),

    CONSTRAINT uq_gts_student_financial_account
        UNIQUE (tenant_id, student_id, currency_code),

    CONSTRAINT ck_gts_student_financial_account_credit
        CHECK (
            credit_balance >= 0
            AND (
                credit_limit IS NULL
                OR credit_limit >= 0
            )
        ),

    CONSTRAINT ck_gts_student_financial_account_dates
        CHECK (
            closed_at IS NULL
            OR closed_at >= opened_at
        ),

    CONSTRAINT ck_gts_student_financial_account_lifecycle
        CHECK (
            billing_status IN (
                'PENDING',
                'ACTIVE',
                'ON_HOLD',
                'DELINQUENT',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_financial_account_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_fee_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    assignment_reference VARCHAR(100) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    fee_structure_id UUID NOT NULL
        REFERENCES gts_fee_structure(id),

    assigned_at TIMESTAMPTZ NOT NULL,
    assigned_by UUID,

    effective_from DATE NOT NULL,
    effective_to DATE,

    workflow_instance_id UUID,

    assignment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_fee_assignment_reference
        UNIQUE (tenant_id, assignment_reference),

    CONSTRAINT uq_gts_student_fee_assignment_scope
        UNIQUE (
            tenant_id,
            student_id,
            fee_structure_id,
            effective_from
        ),

    CONSTRAINT ck_gts_student_fee_assignment_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_student_fee_assignment_lifecycle
        CHECK (
            assignment_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_fee_assignment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_fee_discount_scheme (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    scheme_code VARCHAR(100) NOT NULL,
    scheme_name VARCHAR(250) NOT NULL,
    description VARCHAR(1200),

    discount_type VARCHAR(40) NOT NULL,
    discount_value NUMERIC(18,2) NOT NULL,

    fee_category_id UUID
        REFERENCES gts_fee_category(id),

    fee_item_id UUID
        REFERENCES gts_fee_item(id),

    maximum_discount_amount NUMERIC(18,2),

    eligibility_rules JSONB NOT NULL DEFAULT '{}'::jsonb,

    effective_from DATE NOT NULL,
    effective_to DATE,

    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_fee_discount_scheme_code
        UNIQUE (tenant_id, scheme_code),

    CONSTRAINT ck_gts_fee_discount_type
        CHECK (
            discount_type IN (
                'PERCENTAGE',
                'FIXED_AMOUNT',
                'SIBLING',
                'STAFF_CHILD',
                'EARLY_PAYMENT',
                'SCHOLARSHIP',
                'BURSARY',
                'SPONSORSHIP',
                'WAIVER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_fee_discount_value
        CHECK (
            discount_value >= 0
            AND (
                discount_type <> 'PERCENTAGE'
                OR discount_value <= 100
            )
            AND (
                maximum_discount_amount IS NULL
                OR maximum_discount_amount >= 0
            )
        ),

    CONSTRAINT ck_gts_fee_discount_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_fee_discount_scheme_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_fee_discount (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    discount_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    discount_scheme_id UUID NOT NULL
        REFERENCES gts_fee_discount_scheme(id),

    approved_discount_value NUMERIC(18,2),
    approved_discount_amount NUMERIC(18,2),

    effective_from DATE NOT NULL,
    effective_to DATE,

    evidence_document_id UUID,

    workflow_instance_id UUID,

    requested_at TIMESTAMPTZ,
    requested_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    discount_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_fee_discount_reference
        UNIQUE (tenant_id, discount_reference),

    CONSTRAINT ck_gts_student_fee_discount_values
        CHECK (
            (
                approved_discount_value IS NULL
                OR approved_discount_value >= 0
            )
            AND (
                approved_discount_amount IS NULL
                OR approved_discount_amount >= 0
            )
        ),

    CONSTRAINT ck_gts_student_fee_discount_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_student_fee_discount_approval
        CHECK (
            discount_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_fee_discount_lifecycle
        CHECK (
            discount_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'ACTIVE',
                'EXPIRED',
                'REVOKED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_fee_discount_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_invoice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    invoice_number VARCHAR(100) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    fee_structure_id UUID
        REFERENCES gts_fee_structure(id),

    invoice_date DATE NOT NULL,
    due_date DATE,

    currency_code VARCHAR(3) NOT NULL,

    subtotal_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    outstanding_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    invoice_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    issued_at TIMESTAMPTZ,
    issued_by UUID,

    cancelled_at TIMESTAMPTZ,
    cancelled_by UUID,
    cancellation_reason VARCHAR(1000),

    eds_invoice_document_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_invoice_number
        UNIQUE (tenant_id, invoice_number),

    CONSTRAINT ck_gts_student_invoice_dates
        CHECK (
            due_date IS NULL
            OR due_date >= invoice_date
        ),

    CONSTRAINT ck_gts_student_invoice_amounts
        CHECK (
            subtotal_amount >= 0
            AND discount_amount >= 0
            AND tax_amount >= 0
            AND total_amount >= 0
            AND paid_amount >= 0
            AND outstanding_amount >= 0
            AND paid_amount <= total_amount
            AND outstanding_amount <= total_amount
        ),

    CONSTRAINT ck_gts_student_invoice_issue
        CHECK (
            invoice_status NOT IN (
                'ISSUED',
                'PARTIALLY_PAID',
                'PAID',
                'OVERDUE'
            )
            OR issued_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_invoice_cancel
        CHECK (
            invoice_status <> 'CANCELLED'
            OR cancelled_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_invoice_lifecycle
        CHECK (
            invoice_status IN (
                'DRAFT',
                'ISSUED',
                'PARTIALLY_PAID',
                'PAID',
                'OVERDUE',
                'DISPUTED',
                'CANCELLED',
                'WRITTEN_OFF',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_invoice_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_invoice_line (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    invoice_id UUID NOT NULL
        REFERENCES gts_student_invoice(id) ON DELETE CASCADE,

    fee_item_id UUID
        REFERENCES gts_fee_item(id),

    fee_structure_item_id UUID
        REFERENCES gts_fee_structure_item(id),

    line_description VARCHAR(500) NOT NULL,

    quantity NUMERIC(12,3) NOT NULL DEFAULT 1,
    unit_amount NUMERIC(18,2) NOT NULL,
    gross_amount NUMERIC(18,2) NOT NULL,

    discount_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(18,2) NOT NULL,

    due_date DATE,

    line_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_student_invoice_line_amounts
        CHECK (
            quantity > 0
            AND unit_amount >= 0
            AND gross_amount >= 0
            AND discount_amount >= 0
            AND tax_amount >= 0
            AND net_amount >= 0
        ),

    CONSTRAINT ck_gts_student_invoice_line_lifecycle
        CHECK (
            line_status IN (
                'ACTIVE',
                'PARTIALLY_PAID',
                'PAID',
                'CANCELLED',
                'CREDITED',
                'WRITTEN_OFF'
            )
        ),

    CONSTRAINT ck_gts_student_invoice_line_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_payment_arrangement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    arrangement_reference VARCHAR(100) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    financial_responsibility_id UUID
        REFERENCES gts_student_financial_responsibility(id),

    arrangement_type VARCHAR(40) NOT NULL,

    total_arranged_amount NUMERIC(18,2) NOT NULL,
    deposit_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    installment_count INTEGER,
    installment_frequency VARCHAR(30),

    start_date DATE NOT NULL,
    end_date DATE,

    terms_and_conditions VARCHAR(3000),

    workflow_instance_id UUID,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    arrangement_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_payment_arrangement_reference
        UNIQUE (tenant_id, arrangement_reference),

    CONSTRAINT ck_gts_payment_arrangement_type
        CHECK (
            arrangement_type IN (
                'INSTALLMENT',
                'DEFERRED_PAYMENT',
                'PARTIAL_PAYMENT',
                'SPONSOR_COMMITMENT',
                'SALARY_DEDUCTION',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_payment_arrangement_amounts
        CHECK (
            total_arranged_amount > 0
            AND deposit_amount >= 0
            AND deposit_amount <= total_arranged_amount
        ),

    CONSTRAINT ck_gts_payment_arrangement_installments
        CHECK (
            installment_count IS NULL
            OR installment_count > 0
        ),

    CONSTRAINT ck_gts_payment_arrangement_frequency
        CHECK (
            installment_frequency IS NULL
            OR installment_frequency IN (
                'WEEKLY',
                'FORTNIGHTLY',
                'MONTHLY',
                'TERM',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_payment_arrangement_dates
        CHECK (
            end_date IS NULL
            OR end_date >= start_date
        ),

    CONSTRAINT ck_gts_payment_arrangement_approval
        CHECK (
            arrangement_status NOT IN ('APPROVED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_payment_arrangement_lifecycle
        CHECK (
            arrangement_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'COMPLETED',
                'DEFAULTED',
                'REJECTED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_payment_arrangement_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_payment_installment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    payment_arrangement_id UUID NOT NULL
        REFERENCES gts_payment_arrangement(id) ON DELETE CASCADE,

    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    installment_amount NUMERIC(18,2) NOT NULL,

    paid_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    outstanding_amount NUMERIC(18,2) NOT NULL,

    installment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_payment_installment_number
        UNIQUE (
            tenant_id,
            payment_arrangement_id,
            installment_number
        ),

    CONSTRAINT ck_gts_payment_installment_values
        CHECK (
            installment_number > 0
            AND installment_amount > 0
            AND paid_amount >= 0
            AND outstanding_amount >= 0
            AND paid_amount <= installment_amount
            AND outstanding_amount <= installment_amount
        ),

    CONSTRAINT ck_gts_payment_installment_lifecycle
        CHECK (
            installment_status IN (
                'PENDING',
                'PARTIALLY_PAID',
                'PAID',
                'OVERDUE',
                'WAIVED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_payment_installment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    payment_reference VARCHAR(120) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    payer_guardian_id UUID
        REFERENCES gts_guardian(id),

    financial_responsibility_id UUID
        REFERENCES gts_student_financial_responsibility(id),

    payment_date TIMESTAMPTZ NOT NULL,

    currency_code VARCHAR(3) NOT NULL,
    payment_amount NUMERIC(18,2) NOT NULL,

    payment_method VARCHAR(40) NOT NULL,
    provider_name VARCHAR(120),

    external_transaction_reference VARCHAR(200),
    eip_payment_transaction_id UUID,

    bank_reference VARCHAR(200),
    mobile_money_number VARCHAR(40),
    card_reference VARCHAR(120),

    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    received_by UUID,
    verified_at TIMESTAMPTZ,
    verified_by UUID,

    reversed_at TIMESTAMPTZ,
    reversed_by UUID,
    reversal_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_payment_reference
        UNIQUE (tenant_id, payment_reference),

    CONSTRAINT uq_gts_student_payment_external_reference
        UNIQUE (tenant_id, provider_name, external_transaction_reference),

    CONSTRAINT ck_gts_student_payment_amount
        CHECK (payment_amount > 0),

    CONSTRAINT ck_gts_student_payment_method
        CHECK (
            payment_method IN (
                'CASH',
                'BANK_TRANSFER',
                'BANK_DEPOSIT',
                'MOBILE_MONEY',
                'CARD',
                'CHEQUE',
                'DIRECT_DEBIT',
                'SALARY_DEDUCTION',
                'SPONSOR',
                'SCHOLARSHIP',
                'CREDIT_BALANCE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_payment_verification
        CHECK (
            payment_status NOT IN ('VERIFIED', 'ALLOCATED', 'COMPLETED')
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_payment_reversal
        CHECK (
            payment_status <> 'REVERSED'
            OR reversed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_payment_lifecycle
        CHECK (
            payment_status IN (
                'PENDING',
                'PROCESSING',
                'RECEIVED',
                'VERIFIED',
                'ALLOCATED',
                'COMPLETED',
                'FAILED',
                'REVERSED',
                'REFUNDED',
                'DISPUTED'
            )
        ),

    CONSTRAINT ck_gts_student_payment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_payment_allocation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    student_payment_id UUID NOT NULL
        REFERENCES gts_student_payment(id) ON DELETE CASCADE,

    invoice_id UUID NOT NULL
        REFERENCES gts_student_invoice(id),

    invoice_line_id UUID
        REFERENCES gts_student_invoice_line(id),

    payment_installment_id UUID
        REFERENCES gts_payment_installment(id),

    allocated_amount NUMERIC(18,2) NOT NULL,

    allocated_at TIMESTAMPTZ NOT NULL,
    allocated_by UUID,

    allocation_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_payment_allocation_amount
        CHECK (allocated_amount > 0),

    CONSTRAINT ck_gts_payment_allocation_lifecycle
        CHECK (
            allocation_status IN (
                'ACTIVE',
                'REVERSED',
                'REALLOCATED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_payment_allocation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_payment_receipt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    receipt_number VARCHAR(100) NOT NULL,

    student_payment_id UUID NOT NULL
        REFERENCES gts_student_payment(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    receipt_date TIMESTAMPTZ NOT NULL,

    currency_code VARCHAR(3) NOT NULL,
    receipt_amount NUMERIC(18,2) NOT NULL,

    issued_at TIMESTAMPTZ NOT NULL,
    issued_by UUID,

    eds_receipt_document_id UUID,

    delivery_channel VARCHAR(30),
    delivered_at TIMESTAMPTZ,

    receipt_status VARCHAR(30) NOT NULL DEFAULT 'ISSUED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_payment_receipt_number
        UNIQUE (tenant_id, receipt_number),

    CONSTRAINT uq_gts_payment_receipt_payment
        UNIQUE (tenant_id, student_payment_id),

    CONSTRAINT ck_gts_payment_receipt_amount
        CHECK (receipt_amount > 0),

    CONSTRAINT ck_gts_payment_receipt_channel
        CHECK (
            delivery_channel IS NULL
            OR delivery_channel IN (
                'PRINT',
                'EMAIL',
                'SMS_LINK',
                'WHATSAPP',
                'IN_APP',
                'PORTAL'
            )
        ),

    CONSTRAINT ck_gts_payment_receipt_lifecycle
        CHECK (
            receipt_status IN (
                'ISSUED',
                'DELIVERED',
                'REPRINTED',
                'CANCELLED',
                'VOIDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_payment_receipt_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_financial_adjustment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    adjustment_reference VARCHAR(100) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    invoice_id UUID
        REFERENCES gts_student_invoice(id),

    invoice_line_id UUID
        REFERENCES gts_student_invoice_line(id),

    adjustment_type VARCHAR(40) NOT NULL,
    adjustment_amount NUMERIC(18,2) NOT NULL,

    reason VARCHAR(1500) NOT NULL,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID,

    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    applied_at TIMESTAMPTZ,
    applied_by UUID,

    adjustment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_financial_adjustment_reference
        UNIQUE (tenant_id, adjustment_reference),

    CONSTRAINT ck_gts_financial_adjustment_type
        CHECK (
            adjustment_type IN (
                'CREDIT_NOTE',
                'DEBIT_NOTE',
                'WAIVER',
                'WRITE_OFF',
                'CORRECTION',
                'PENALTY',
                'INTEREST',
                'REVERSAL',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_financial_adjustment_amount
        CHECK (adjustment_amount > 0),

    CONSTRAINT ck_gts_financial_adjustment_approval
        CHECK (
            adjustment_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_financial_adjustment_applied
        CHECK (
            adjustment_status <> 'APPLIED'
            OR applied_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_financial_adjustment_lifecycle
        CHECK (
            adjustment_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'REVERSED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_financial_adjustment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_refund (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    refund_reference VARCHAR(100) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    student_payment_id UUID
        REFERENCES gts_student_payment(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    refund_amount NUMERIC(18,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,

    refund_reason VARCHAR(1500) NOT NULL,
    refund_method VARCHAR(40) NOT NULL,

    provider_name VARCHAR(120),
    external_refund_reference VARCHAR(200),
    eip_refund_transaction_id UUID,

    workflow_instance_id UUID,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    processed_at TIMESTAMPTZ,
    processed_by UUID,

    refund_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_refund_reference
        UNIQUE (tenant_id, refund_reference),

    CONSTRAINT ck_gts_student_refund_amount
        CHECK (refund_amount > 0),

    CONSTRAINT ck_gts_student_refund_method
        CHECK (
            refund_method IN (
                'CASH',
                'BANK_TRANSFER',
                'MOBILE_MONEY',
                'CARD_REVERSAL',
                'CHEQUE',
                'ACCOUNT_CREDIT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_refund_approval
        CHECK (
            refund_status NOT IN ('APPROVED', 'PROCESSING', 'COMPLETED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_refund_processing
        CHECK (
            refund_status <> 'COMPLETED'
            OR processed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_refund_lifecycle
        CHECK (
            refund_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'PROCESSING',
                'COMPLETED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_student_refund_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_finance_reminder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    reminder_reference VARCHAR(100) NOT NULL,

    student_financial_account_id UUID NOT NULL
        REFERENCES gts_student_financial_account(id),

    invoice_id UUID
        REFERENCES gts_student_invoice(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    reminder_type VARCHAR(40) NOT NULL,
    reminder_date TIMESTAMPTZ NOT NULL,

    amount_due NUMERIC(18,2),
    days_overdue INTEGER,

    communication_channel VARCHAR(30) NOT NULL,

    notification_request_id UUID,
    reminder_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    failure_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_finance_reminder_reference
        UNIQUE (tenant_id, reminder_reference),

    CONSTRAINT ck_gts_finance_reminder_type
        CHECK (
            reminder_type IN (
                'UPCOMING_DUE_DATE',
                'DUE_TODAY',
                'OVERDUE',
                'PAYMENT_PLAN_DUE',
                'PAYMENT_PLAN_DEFAULT',
                'ACCOUNT_HOLD',
                'FINAL_NOTICE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_finance_reminder_values
        CHECK (
            (amount_due IS NULL OR amount_due >= 0)
            AND (days_overdue IS NULL OR days_overdue >= 0)
        ),

    CONSTRAINT ck_gts_finance_reminder_channel
        CHECK (
            communication_channel IN (
                'SMS',
                'EMAIL',
                'PUSH',
                'WHATSAPP',
                'VOICE_CALL',
                'IN_APP'
            )
        ),

    CONSTRAINT ck_gts_finance_reminder_lifecycle
        CHECK (
            reminder_status IN (
                'PENDING',
                'QUEUED',
                'SENT',
                'DELIVERED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_finance_reminder_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_finance_reconciliation_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    reconciliation_reference VARCHAR(100) NOT NULL,

    reconciliation_date DATE NOT NULL,
    provider_name VARCHAR(120) NOT NULL,
    settlement_reference VARCHAR(200),

    expected_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    received_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    difference_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    transaction_count INTEGER NOT NULL DEFAULT 0,
    matched_count INTEGER NOT NULL DEFAULT 0,
    unmatched_count INTEGER NOT NULL DEFAULT 0,

    eip_reconciliation_id UUID,

    reconciled_at TIMESTAMPTZ,
    reconciled_by UUID,

    reconciliation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_finance_reconciliation_reference
        UNIQUE (tenant_id, reconciliation_reference),

    CONSTRAINT ck_gts_finance_reconciliation_amounts
        CHECK (
            expected_amount >= 0
            AND received_amount >= 0
        ),

    CONSTRAINT ck_gts_finance_reconciliation_counts
        CHECK (
            transaction_count >= 0
            AND matched_count >= 0
            AND unmatched_count >= 0
            AND matched_count + unmatched_count <= transaction_count
        ),

    CONSTRAINT ck_gts_finance_reconciliation_completion
        CHECK (
            reconciliation_status <> 'RECONCILED'
            OR reconciled_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_finance_reconciliation_lifecycle
        CHECK (
            reconciliation_status IN (
                'PENDING',
                'PROCESSING',
                'PARTIALLY_MATCHED',
                'RECONCILED',
                'DISPUTED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_finance_reconciliation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_finance_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,

    event_type VARCHAR(50) NOT NULL,
    event_description VARCHAR(1500),

    previous_value JSONB,
    new_value JSONB,

    effective_at TIMESTAMPTZ NOT NULL,
    event_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_finance_history_entity
        CHECK (
            entity_type IN (
                'FEE_CATEGORY',
                'FEE_ITEM',
                'FEE_STRUCTURE',
                'FINANCIAL_ACCOUNT',
                'FEE_ASSIGNMENT',
                'DISCOUNT',
                'INVOICE',
                'PAYMENT_ARRANGEMENT',
                'PAYMENT',
                'ALLOCATION',
                'RECEIPT',
                'ADJUSTMENT',
                'REFUND',
                'REMINDER',
                'RECONCILIATION'
            )
        ),

    CONSTRAINT ck_gts_finance_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'ASSIGNED',
                'ISSUED',
                'APPROVED',
                'REJECTED',
                'PAYMENT_RECEIVED',
                'PAYMENT_VERIFIED',
                'PAYMENT_ALLOCATED',
                'RECEIPT_ISSUED',
                'DISCOUNT_APPLIED',
                'ARRANGEMENT_APPROVED',
                'ARRANGEMENT_DEFAULTED',
                'ADJUSTMENT_APPLIED',
                'REFUND_REQUESTED',
                'REFUND_COMPLETED',
                'REMINDER_SENT',
                'RECONCILED',
                'DISPUTED',
                'REVERSED',
                'CANCELLED',
                'WRITTEN_OFF',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_fee_item_category
    ON gts_fee_item (
        tenant_id,
        fee_category_id,
        active
    );

CREATE INDEX ix_gts_fee_structure_period
    ON gts_fee_structure (
        tenant_id,
        academic_year_id,
        academic_term_id,
        class_grade_id,
        structure_status
    );

CREATE INDEX ix_gts_fee_structure_item_structure
    ON gts_fee_structure_item (
        tenant_id,
        fee_structure_id,
        sequence_number
    );

CREATE INDEX ix_gts_student_financial_account_student
    ON gts_student_financial_account (
        tenant_id,
        student_id,
        billing_status
    );

CREATE INDEX ix_gts_student_fee_assignment_student
    ON gts_student_fee_assignment (
        tenant_id,
        student_id,
        assignment_status
    );

CREATE INDEX ix_gts_student_fee_discount_student
    ON gts_student_fee_discount (
        tenant_id,
        student_id,
        discount_status
    );

CREATE INDEX ix_gts_student_invoice_account
    ON gts_student_invoice (
        tenant_id,
        student_financial_account_id,
        invoice_status,
        due_date
    );

CREATE INDEX ix_gts_student_invoice_student
    ON gts_student_invoice (
        tenant_id,
        student_id,
        academic_year_id,
        academic_term_id
    );

CREATE INDEX ix_gts_student_invoice_line_invoice
    ON gts_student_invoice_line (
        tenant_id,
        invoice_id,
        line_status
    );

CREATE INDEX ix_gts_payment_arrangement_account
    ON gts_payment_arrangement (
        tenant_id,
        student_financial_account_id,
        arrangement_status
    );

CREATE INDEX ix_gts_payment_installment_due
    ON gts_payment_installment (
        tenant_id,
        due_date,
        installment_status
    );

CREATE INDEX ix_gts_student_payment_account
    ON gts_student_payment (
        tenant_id,
        student_financial_account_id,
        payment_date,
        payment_status
    );

CREATE INDEX ix_gts_student_payment_external
    ON gts_student_payment (
        tenant_id,
        provider_name,
        external_transaction_reference
    );

CREATE INDEX ix_gts_payment_allocation_payment
    ON gts_payment_allocation (
        tenant_id,
        student_payment_id,
        allocation_status
    );

CREATE INDEX ix_gts_payment_allocation_invoice
    ON gts_payment_allocation (
        tenant_id,
        invoice_id,
        allocation_status
    );

CREATE INDEX ix_gts_financial_adjustment_account
    ON gts_financial_adjustment (
        tenant_id,
        student_financial_account_id,
        adjustment_status
    );

CREATE INDEX ix_gts_student_refund_account
    ON gts_student_refund (
        tenant_id,
        student_financial_account_id,
        refund_status
    );

CREATE INDEX ix_gts_finance_reminder_due
    ON gts_finance_reminder (
        tenant_id,
        reminder_date,
        reminder_status
    );

CREATE INDEX ix_gts_finance_reconciliation_date
    ON gts_finance_reconciliation_record (
        tenant_id,
        reconciliation_date,
        provider_name,
        reconciliation_status
    );

CREATE INDEX ix_gts_finance_history_entity
    ON gts_finance_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '044',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
