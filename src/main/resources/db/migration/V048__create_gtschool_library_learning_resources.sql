CREATE TABLE gts_library (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    library_code VARCHAR(80) NOT NULL,
    library_name VARCHAR(250) NOT NULL,
    description VARCHAR(1200),

    library_type VARCHAR(40) NOT NULL,
    location_description VARCHAR(300),

    maximum_capacity INTEGER,
    allows_student_borrowing BOOLEAN NOT NULL DEFAULT TRUE,
    allows_staff_borrowing BOOLEAN NOT NULL DEFAULT TRUE,
    allows_digital_access BOOLEAN NOT NULL DEFAULT TRUE,

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    library_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_code
        UNIQUE (tenant_id, library_code),

    CONSTRAINT ck_gts_library_type
        CHECK (
            library_type IN (
                'MAIN_LIBRARY',
                'CAMPUS_LIBRARY',
                'CLASS_LIBRARY',
                'DEPARTMENT_LIBRARY',
                'DIGITAL_LIBRARY',
                'RESOURCE_CENTRE',
                'TEXTBOOK_STORE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_library_capacity
        CHECK (
            maximum_capacity IS NULL
            OR maximum_capacity > 0
        ),

    CONSTRAINT ck_gts_library_lifecycle
        CHECK (
            library_status IN (
                'PLANNED',
                'ACTIVE',
                'TEMPORARILY_CLOSED',
                'MAINTENANCE',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_library_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_learning_resource_title (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    resource_code VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    subtitle VARCHAR(500),

    resource_type VARCHAR(40) NOT NULL,

    isbn VARCHAR(40),
    issn VARCHAR(40),
    edition VARCHAR(80),
    publication_year INTEGER,

    publisher_name VARCHAR(250),
    publication_place VARCHAR(250),

    language_code VARCHAR(10),
    description VARCHAR(3000),

    subject_id UUID
        REFERENCES gts_subject(id),

    academic_department_id UUID
        REFERENCES gts_academic_department(id),

    education_level_id UUID
        REFERENCES gts_education_level(id),

    curriculum_version_id UUID
        REFERENCES gts_curriculum_version(id),

    classification_code VARCHAR(120),
    call_number_prefix VARCHAR(120),

    age_restriction VARCHAR(80),
    restricted_access BOOLEAN NOT NULL DEFAULT FALSE,

    digital_resource BOOLEAN NOT NULL DEFAULT FALSE,
    eds_digital_document_id UUID,
    external_resource_url VARCHAR(1000),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_learning_resource_code
        UNIQUE (tenant_id, resource_code),

    CONSTRAINT uq_gts_learning_resource_isbn
        UNIQUE (tenant_id, isbn),

    CONSTRAINT ck_gts_learning_resource_type
        CHECK (
            resource_type IN (
                'BOOK',
                'TEXTBOOK',
                'REFERENCE_BOOK',
                'JOURNAL',
                'MAGAZINE',
                'NEWSPAPER',
                'EBOOK',
                'AUDIOBOOK',
                'VIDEO',
                'AUDIO',
                'MAP',
                'CHART',
                'TEACHING_AID',
                'PAST_PAPER',
                'RESEARCH_PAPER',
                'DIGITAL_RESOURCE',
                'EQUIPMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_learning_resource_year
        CHECK (
            publication_year IS NULL
            OR publication_year >= 1000
        ),

    CONSTRAINT ck_gts_learning_resource_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_learning_resource_author (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    author_name VARCHAR(250) NOT NULL,
    author_type VARCHAR(30) NOT NULL DEFAULT 'PERSON',

    biography VARCHAR(2000),
    country_code VARCHAR(3),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_learning_resource_author_type
        CHECK (
            author_type IN (
                'PERSON',
                'ORGANIZATION',
                'GOVERNMENT',
                'EDITORIAL_TEAM',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_learning_resource_author_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_learning_resource_author_link (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    learning_resource_title_id UUID NOT NULL
        REFERENCES gts_learning_resource_title(id) ON DELETE CASCADE,

    author_id UUID NOT NULL
        REFERENCES gts_learning_resource_author(id),

    contribution_type VARCHAR(30) NOT NULL DEFAULT 'AUTHOR',
    sequence_number INTEGER NOT NULL DEFAULT 1,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_learning_resource_author_link
        UNIQUE (
            tenant_id,
            learning_resource_title_id,
            author_id,
            contribution_type
        ),

    CONSTRAINT ck_gts_resource_contribution_type
        CHECK (
            contribution_type IN (
                'AUTHOR',
                'EDITOR',
                'ILLUSTRATOR',
                'TRANSLATOR',
                'COMPILER',
                'CONTRIBUTOR',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_resource_author_sequence
        CHECK (sequence_number > 0),

    CONSTRAINT ck_gts_resource_author_link_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_learning_resource_copy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    library_id UUID NOT NULL
        REFERENCES gts_library(id),

    learning_resource_title_id UUID NOT NULL
        REFERENCES gts_learning_resource_title(id),

    copy_number VARCHAR(100) NOT NULL,
    barcode VARCHAR(160),
    rfid_tag VARCHAR(160),

    acquisition_type VARCHAR(30) NOT NULL,
    acquisition_date DATE,
    acquisition_cost NUMERIC(18,2),
    currency_code VARCHAR(3),

    supplier_name VARCHAR(250),
    supplier_reference VARCHAR(160),

    shelf_location VARCHAR(160),
    call_number VARCHAR(160),

    condition_status VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    circulation_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

    replacement_cost NUMERIC(18,2),

    last_inventory_date DATE,
    notes VARCHAR(1500),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_learning_resource_copy_number
        UNIQUE (tenant_id, library_id, copy_number),

    CONSTRAINT uq_gts_learning_resource_barcode
        UNIQUE (tenant_id, barcode),

    CONSTRAINT uq_gts_learning_resource_rfid
        UNIQUE (tenant_id, rfid_tag),

    CONSTRAINT ck_gts_learning_resource_acquisition_type
        CHECK (
            acquisition_type IN (
                'PURCHASE',
                'DONATION',
                'TRANSFER',
                'LEASE',
                'GOVERNMENT_SUPPLY',
                'PROJECT_SUPPLY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_learning_resource_cost
        CHECK (
            (acquisition_cost IS NULL OR acquisition_cost >= 0)
            AND (replacement_cost IS NULL OR replacement_cost >= 0)
        ),

    CONSTRAINT ck_gts_learning_resource_condition
        CHECK (
            condition_status IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED',
                'LOST',
                'WITHDRAWN'
            )
        ),

    CONSTRAINT ck_gts_learning_resource_circulation
        CHECK (
            circulation_status IN (
                'AVAILABLE',
                'ON_LOAN',
                'RESERVED',
                'PROCESSING',
                'REPAIR',
                'LOST',
                'DAMAGED',
                'WITHDRAWN'
            )
        ),

    CONSTRAINT ck_gts_learning_resource_copy_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    membership_number VARCHAR(100) NOT NULL,

    library_id UUID NOT NULL
        REFERENCES gts_library(id),

    member_type VARCHAR(30) NOT NULL,

    student_id UUID
        REFERENCES gts_student(id),

    workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    maximum_active_loans INTEGER NOT NULL DEFAULT 3,
    maximum_loan_days INTEGER NOT NULL DEFAULT 14,
    renewal_limit INTEGER NOT NULL DEFAULT 1,

    borrowing_suspended BOOLEAN NOT NULL DEFAULT FALSE,
    suspension_reason VARCHAR(1000),

    effective_from DATE NOT NULL,
    effective_to DATE,

    membership_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_membership_number
        UNIQUE (tenant_id, membership_number),

    CONSTRAINT ck_gts_library_member_type
        CHECK (
            member_type IN (
                'STUDENT',
                'STAFF',
                'GUARDIAN',
                'ALUMNI',
                'COMMUNITY',
                'VISITOR',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_library_membership_limits
        CHECK (
            maximum_active_loans >= 0
            AND maximum_loan_days > 0
            AND renewal_limit >= 0
        ),

    CONSTRAINT ck_gts_library_membership_dates
        CHECK (
            effective_to IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_library_membership_lifecycle
        CHECK (
            membership_status IN (
                'PENDING',
                'ACTIVE',
                'SUSPENDED',
                'EXPIRED',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_library_membership_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_loan (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    loan_reference VARCHAR(100) NOT NULL,

    library_membership_id UUID NOT NULL
        REFERENCES gts_library_membership(id),

    learning_resource_copy_id UUID NOT NULL
        REFERENCES gts_learning_resource_copy(id),

    issued_at TIMESTAMPTZ NOT NULL,
    issued_by UUID,

    due_at TIMESTAMPTZ NOT NULL,

    returned_at TIMESTAMPTZ,
    returned_to UUID,

    renewal_count INTEGER NOT NULL DEFAULT 0,

    condition_at_issue VARCHAR(30),
    condition_at_return VARCHAR(30),

    overdue_days INTEGER NOT NULL DEFAULT 0,
    fine_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    loan_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_loan_reference
        UNIQUE (tenant_id, loan_reference),

    CONSTRAINT ck_gts_library_loan_dates
        CHECK (
            due_at >= issued_at
            AND (
                returned_at IS NULL
                OR returned_at >= issued_at
            )
        ),

    CONSTRAINT ck_gts_library_loan_values
        CHECK (
            renewal_count >= 0
            AND overdue_days >= 0
            AND fine_amount >= 0
        ),

    CONSTRAINT ck_gts_library_loan_condition_issue
        CHECK (
            condition_at_issue IS NULL
            OR condition_at_issue IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED'
            )
        ),

    CONSTRAINT ck_gts_library_loan_condition_return
        CHECK (
            condition_at_return IS NULL
            OR condition_at_return IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED',
                'LOST'
            )
        ),

    CONSTRAINT ck_gts_library_loan_return
        CHECK (
            loan_status <> 'RETURNED'
            OR returned_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_library_loan_lifecycle
        CHECK (
            loan_status IN (
                'ACTIVE',
                'OVERDUE',
                'RENEWED',
                'RETURNED',
                'LOST',
                'DAMAGED',
                'RECALLED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_library_loan_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_reservation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    reservation_reference VARCHAR(100) NOT NULL,

    library_membership_id UUID NOT NULL
        REFERENCES gts_library_membership(id),

    learning_resource_title_id UUID NOT NULL
        REFERENCES gts_learning_resource_title(id),

    preferred_library_id UUID
        REFERENCES gts_library(id),

    reserved_at TIMESTAMPTZ NOT NULL,
    queue_position INTEGER,

    available_at TIMESTAMPTZ,
    collection_deadline TIMESTAMPTZ,

    fulfilled_loan_id UUID
        REFERENCES gts_library_loan(id),

    reservation_status VARCHAR(30) NOT NULL DEFAULT 'WAITING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_reservation_reference
        UNIQUE (tenant_id, reservation_reference),

    CONSTRAINT uq_gts_library_active_reservation
        UNIQUE (
            tenant_id,
            library_membership_id,
            learning_resource_title_id
        ),

    CONSTRAINT ck_gts_library_reservation_queue
        CHECK (
            queue_position IS NULL
            OR queue_position > 0
        ),

    CONSTRAINT ck_gts_library_reservation_deadline
        CHECK (
            collection_deadline IS NULL
            OR available_at IS NULL
            OR collection_deadline >= available_at
        ),

    CONSTRAINT ck_gts_library_reservation_lifecycle
        CHECK (
            reservation_status IN (
                'WAITING',
                'AVAILABLE',
                'COLLECTED',
                'FULFILLED',
                'EXPIRED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_library_reservation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_textbook_issuance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    issuance_reference VARCHAR(100) NOT NULL,

    learning_resource_copy_id UUID NOT NULL
        REFERENCES gts_learning_resource_copy(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    subject_id UUID
        REFERENCES gts_subject(id),

    issued_at TIMESTAMPTZ NOT NULL,
    issued_by UUID,

    expected_return_date DATE,
    returned_at TIMESTAMPTZ,
    returned_to UUID,

    condition_at_issue VARCHAR(30),
    condition_at_return VARCHAR(30),

    replacement_charge NUMERIC(18,2) NOT NULL DEFAULT 0,
    financial_adjustment_id UUID
        REFERENCES gts_financial_adjustment(id),

    issuance_status VARCHAR(30) NOT NULL DEFAULT 'ISSUED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_textbook_issuance_reference
        UNIQUE (tenant_id, issuance_reference),

    CONSTRAINT uq_gts_active_textbook_issuance
        UNIQUE (
            tenant_id,
            learning_resource_copy_id,
            academic_year_id,
            student_id
        ),

    CONSTRAINT ck_gts_textbook_issuance_dates
        CHECK (
            returned_at IS NULL
            OR returned_at >= issued_at
        ),

    CONSTRAINT ck_gts_textbook_issuance_charge
        CHECK (replacement_charge >= 0),

    CONSTRAINT ck_gts_textbook_condition_issue
        CHECK (
            condition_at_issue IS NULL
            OR condition_at_issue IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED'
            )
        ),

    CONSTRAINT ck_gts_textbook_condition_return
        CHECK (
            condition_at_return IS NULL
            OR condition_at_return IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED',
                'LOST'
            )
        ),

    CONSTRAINT ck_gts_textbook_issuance_lifecycle
        CHECK (
            issuance_status IN (
                'ISSUED',
                'RETURNED',
                'OVERDUE',
                'LOST',
                'DAMAGED',
                'REPLACED',
                'CHARGED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_textbook_issuance_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_fine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    fine_reference VARCHAR(100) NOT NULL,

    library_membership_id UUID NOT NULL
        REFERENCES gts_library_membership(id),

    library_loan_id UUID
        REFERENCES gts_library_loan(id),

    textbook_issuance_id UUID
        REFERENCES gts_textbook_issuance(id),

    fine_type VARCHAR(40) NOT NULL,
    fine_amount NUMERIC(18,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,

    fine_reason VARCHAR(1200),

    assessed_at TIMESTAMPTZ NOT NULL,
    assessed_by UUID,

    financial_adjustment_id UUID
        REFERENCES gts_financial_adjustment(id),

    waived_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

    fine_status VARCHAR(30) NOT NULL DEFAULT 'ASSESSED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_fine_reference
        UNIQUE (tenant_id, fine_reference),

    CONSTRAINT ck_gts_library_fine_type
        CHECK (
            fine_type IN (
                'OVERDUE',
                'LOST_RESOURCE',
                'DAMAGED_RESOURCE',
                'REPLACEMENT',
                'PROCESSING',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_library_fine_amounts
        CHECK (
            fine_amount > 0
            AND waived_amount >= 0
            AND paid_amount >= 0
            AND waived_amount <= fine_amount
            AND paid_amount <= fine_amount
        ),

    CONSTRAINT ck_gts_library_fine_lifecycle
        CHECK (
            fine_status IN (
                'ASSESSED',
                'PARTIALLY_PAID',
                'PAID',
                'WAIVED',
                'CANCELLED',
                'WRITTEN_OFF'
            )
        ),

    CONSTRAINT ck_gts_library_fine_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_inventory_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    inventory_reference VARCHAR(100) NOT NULL,

    library_id UUID NOT NULL
        REFERENCES gts_library(id),

    inventory_type VARCHAR(30) NOT NULL,

    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    started_by UUID,
    completed_by UUID,

    expected_copy_count INTEGER NOT NULL DEFAULT 0,
    counted_copy_count INTEGER NOT NULL DEFAULT 0,
    missing_copy_count INTEGER NOT NULL DEFAULT 0,
    damaged_copy_count INTEGER NOT NULL DEFAULT 0,

    workflow_instance_id UUID,

    inventory_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_inventory_reference
        UNIQUE (tenant_id, inventory_reference),

    CONSTRAINT ck_gts_library_inventory_type
        CHECK (
            inventory_type IN (
                'FULL_STOCKTAKE',
                'PARTIAL_STOCKTAKE',
                'SHELF_CHECK',
                'TEXTBOOK_CHECK',
                'AUDIT',
                'TRANSFER_CHECK'
            )
        ),

    CONSTRAINT ck_gts_library_inventory_dates
        CHECK (
            completed_at IS NULL
            OR completed_at >= started_at
        ),

    CONSTRAINT ck_gts_library_inventory_counts
        CHECK (
            expected_copy_count >= 0
            AND counted_copy_count >= 0
            AND missing_copy_count >= 0
            AND damaged_copy_count >= 0
        ),

    CONSTRAINT ck_gts_library_inventory_completion
        CHECK (
            inventory_status <> 'COMPLETED'
            OR completed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_library_inventory_lifecycle
        CHECK (
            inventory_status IN (
                'OPEN',
                'IN_PROGRESS',
                'RECONCILIATION',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_library_inventory_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_inventory_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    inventory_session_id UUID NOT NULL
        REFERENCES gts_library_inventory_session(id) ON DELETE CASCADE,

    learning_resource_copy_id UUID NOT NULL
        REFERENCES gts_learning_resource_copy(id),

    expected_location VARCHAR(160),
    observed_location VARCHAR(160),

    observed_condition VARCHAR(30),
    inventory_outcome VARCHAR(30) NOT NULL,

    scanned_at TIMESTAMPTZ,
    scanned_by UUID,

    reconciliation_notes VARCHAR(1200),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_library_inventory_item
        UNIQUE (
            tenant_id,
            inventory_session_id,
            learning_resource_copy_id
        ),

    CONSTRAINT ck_gts_library_inventory_condition
        CHECK (
            observed_condition IS NULL
            OR observed_condition IN (
                'NEW',
                'GOOD',
                'FAIR',
                'POOR',
                'DAMAGED',
                'LOST'
            )
        ),

    CONSTRAINT ck_gts_library_inventory_outcome
        CHECK (
            inventory_outcome IN (
                'FOUND',
                'MISSING',
                'MISPLACED',
                'DAMAGED',
                'ON_LOAN',
                'WITHDRAWN',
                'NOT_CHECKED'
            )
        ),

    CONSTRAINT ck_gts_library_inventory_item_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    notification_reference VARCHAR(100) NOT NULL,

    library_membership_id UUID NOT NULL
        REFERENCES gts_library_membership(id),

    library_loan_id UUID
        REFERENCES gts_library_loan(id),

    reservation_id UUID
        REFERENCES gts_library_reservation(id),

    textbook_issuance_id UUID
        REFERENCES gts_textbook_issuance(id),

    notification_type VARCHAR(40) NOT NULL,
    communication_channel VARCHAR(30) NOT NULL,

    notification_request_id UUID,

    notification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    queued_at TIMESTAMPTZ,
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

    CONSTRAINT uq_gts_library_notification_reference
        UNIQUE (tenant_id, notification_reference),

    CONSTRAINT ck_gts_library_notification_type
        CHECK (
            notification_type IN (
                'LOAN_DUE',
                'LOAN_OVERDUE',
                'RESERVATION_AVAILABLE',
                'RESERVATION_EXPIRING',
                'TEXTBOOK_RETURN_DUE',
                'LOST_RESOURCE',
                'DAMAGED_RESOURCE',
                'FINE_ASSESSED',
                'FINE_OUTSTANDING',
                'RECALL_NOTICE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_library_notification_channel
        CHECK (
            communication_channel IN (
                'SMS',
                'EMAIL',
                'PUSH',
                'WHATSAPP',
                'IN_APP',
                'LETTER'
            )
        ),

    CONSTRAINT ck_gts_library_notification_lifecycle
        CHECK (
            notification_status IN (
                'PENDING',
                'QUEUED',
                'SENT',
                'DELIVERED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_library_notification_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_library_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,

    event_type VARCHAR(50) NOT NULL,
    event_description VARCHAR(2000),

    previous_value JSONB,
    new_value JSONB,

    effective_at TIMESTAMPTZ NOT NULL,
    event_by UUID,

    workflow_instance_id UUID,
    correlation_id VARCHAR(120),

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_library_history_entity
        CHECK (
            entity_type IN (
                'LIBRARY',
                'RESOURCE_TITLE',
                'RESOURCE_AUTHOR',
                'RESOURCE_COPY',
                'MEMBERSHIP',
                'LOAN',
                'RESERVATION',
                'TEXTBOOK_ISSUANCE',
                'FINE',
                'INVENTORY_SESSION',
                'INVENTORY_ITEM',
                'NOTIFICATION'
            )
        ),

    CONSTRAINT ck_gts_library_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'UPDATED',
                'ACQUIRED',
                'CATALOGUED',
                'ISSUED',
                'RENEWED',
                'RETURNED',
                'RESERVED',
                'RESERVATION_FULFILLED',
                'OVERDUE',
                'LOST',
                'DAMAGED',
                'FINE_ASSESSED',
                'FINE_PAID',
                'FINE_WAIVED',
                'INVENTORY_STARTED',
                'INVENTORY_COMPLETED',
                'COPY_FOUND',
                'COPY_MISSING',
                'NOTIFICATION_SENT',
                'TRANSFERRED',
                'WITHDRAWN',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_library_campus
    ON gts_library (
        tenant_id,
        campus_id,
        library_type,
        library_status
    );

CREATE INDEX ix_gts_learning_resource_subject
    ON gts_learning_resource_title (
        tenant_id,
        subject_id,
        resource_type,
        active
    );

CREATE INDEX ix_gts_learning_resource_copy_title
    ON gts_learning_resource_copy (
        tenant_id,
        learning_resource_title_id,
        circulation_status
    );

CREATE INDEX ix_gts_learning_resource_copy_library
    ON gts_learning_resource_copy (
        tenant_id,
        library_id,
        shelf_location,
        circulation_status
    );

CREATE INDEX ix_gts_library_membership_student
    ON gts_library_membership (
        tenant_id,
        student_id,
        membership_status
    );

CREATE INDEX ix_gts_library_membership_workforce
    ON gts_library_membership (
        tenant_id,
        workforce_member_id,
        membership_status
    );

CREATE INDEX ix_gts_library_loan_member
    ON gts_library_loan (
        tenant_id,
        library_membership_id,
        loan_status,
        due_at
    );

CREATE INDEX ix_gts_library_loan_copy
    ON gts_library_loan (
        tenant_id,
        learning_resource_copy_id,
        loan_status
    );

CREATE INDEX ix_gts_library_reservation_title
    ON gts_library_reservation (
        tenant_id,
        learning_resource_title_id,
        reservation_status,
        queue_position
    );

CREATE INDEX ix_gts_textbook_issuance_student
    ON gts_textbook_issuance (
        tenant_id,
        student_id,
        academic_year_id,
        issuance_status
    );

CREATE INDEX ix_gts_library_fine_member
    ON gts_library_fine (
        tenant_id,
        library_membership_id,
        fine_status
    );

CREATE INDEX ix_gts_library_inventory_session_library
    ON gts_library_inventory_session (
        tenant_id,
        library_id,
        inventory_status,
        started_at
    );

CREATE INDEX ix_gts_library_inventory_item_session
    ON gts_library_inventory_item (
        tenant_id,
        inventory_session_id,
        inventory_outcome
    );

CREATE INDEX ix_gts_library_notification_member
    ON gts_library_notification (
        tenant_id,
        library_membership_id,
        notification_status
    );

CREATE INDEX ix_gts_library_history_entity
    ON gts_library_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '048',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
