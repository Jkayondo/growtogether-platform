CREATE TABLE gts_mark_sheet (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    mark_sheet_reference VARCHAR(100) NOT NULL,

    assessment_component_id UUID NOT NULL
        REFERENCES gts_assessment_component(id),

    assessment_paper_id UUID
        REFERENCES gts_assessment_paper(id),

    examination_schedule_id UUID
        REFERENCES gts_examination_schedule(id),

    subject_offering_id UUID NOT NULL
        REFERENCES gts_subject_offering(id),

    class_offering_id UUID NOT NULL
        REFERENCES gts_class_offering(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    teacher_profile_id UUID
        REFERENCES gts_teacher_profile(id),

    maximum_score NUMERIC(10,2) NOT NULL,
    pass_score NUMERIC(10,2),

    expected_candidate_count INTEGER NOT NULL DEFAULT 0,
    recorded_candidate_count INTEGER NOT NULL DEFAULT 0,

    entry_opened_at TIMESTAMPTZ,
    entry_opened_by UUID,
    submission_deadline TIMESTAMPTZ,

    submitted_at TIMESTAMPTZ,
    submitted_by UUID,

    locked_at TIMESTAMPTZ,
    locked_by UUID,
    lock_reason VARCHAR(1000),

    workflow_instance_id UUID,

    mark_sheet_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_mark_sheet_reference
        UNIQUE (tenant_id, mark_sheet_reference),

    CONSTRAINT uq_gts_mark_sheet_scope
        UNIQUE (
            tenant_id,
            assessment_component_id,
            assessment_paper_id,
            class_offering_id,
            stream_id
        ),

    CONSTRAINT ck_gts_mark_sheet_scores
        CHECK (
            maximum_score > 0
            AND (
                pass_score IS NULL
                OR (
                    pass_score >= 0
                    AND pass_score <= maximum_score
                )
            )
        ),

    CONSTRAINT ck_gts_mark_sheet_counts
        CHECK (
            expected_candidate_count >= 0
            AND recorded_candidate_count >= 0
            AND recorded_candidate_count <= expected_candidate_count
        ),

    CONSTRAINT ck_gts_mark_sheet_submission
        CHECK (
            mark_sheet_status NOT IN (
                'SUBMITTED',
                'VALIDATED',
                'MODERATION',
                'APPROVED',
                'LOCKED',
                'PUBLISHED'
            )
            OR submitted_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_mark_sheet_lock
        CHECK (
            mark_sheet_status NOT IN ('LOCKED', 'PUBLISHED')
            OR locked_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_mark_sheet_lifecycle
        CHECK (
            mark_sheet_status IN (
                'DRAFT',
                'OPEN',
                'IN_PROGRESS',
                'SUBMITTED',
                'VALIDATION_FAILED',
                'VALIDATED',
                'MODERATION',
                'RETURNED',
                'APPROVED',
                'LOCKED',
                'PUBLISHED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_mark_sheet_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_mark_entry_batch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    batch_reference VARCHAR(100) NOT NULL,

    mark_sheet_id UUID NOT NULL
        REFERENCES gts_mark_sheet(id) ON DELETE CASCADE,

    batch_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL',

    source_file_name VARCHAR(250),
    source_document_id UUID,

    total_records INTEGER NOT NULL DEFAULT 0,
    accepted_records INTEGER NOT NULL DEFAULT 0,
    rejected_records INTEGER NOT NULL DEFAULT 0,

    imported_at TIMESTAMPTZ,
    imported_by UUID,

    validation_summary JSONB NOT NULL DEFAULT '{}'::jsonb,

    batch_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_mark_entry_batch_reference
        UNIQUE (tenant_id, batch_reference),

    CONSTRAINT ck_gts_mark_entry_batch_type
        CHECK (
            batch_type IN (
                'MANUAL',
                'BULK_IMPORT',
                'OFFLINE_SYNC',
                'API',
                'MIGRATION',
                'SYSTEM'
            )
        ),

    CONSTRAINT ck_gts_mark_entry_batch_counts
        CHECK (
            total_records >= 0
            AND accepted_records >= 0
            AND rejected_records >= 0
            AND accepted_records + rejected_records <= total_records
        ),

    CONSTRAINT ck_gts_mark_entry_batch_lifecycle
        CHECK (
            batch_status IN (
                'PENDING',
                'VALIDATING',
                'VALIDATED',
                'PARTIALLY_ACCEPTED',
                'ACCEPTED',
                'REJECTED',
                'APPLIED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_mark_entry_batch_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_candidate_score (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    mark_sheet_id UUID NOT NULL
        REFERENCES gts_mark_sheet(id) ON DELETE CASCADE,

    mark_entry_batch_id UUID
        REFERENCES gts_mark_entry_batch(id),

    examination_candidate_id UUID
        REFERENCES gts_examination_candidate(id),

    candidate_paper_registration_id UUID
        REFERENCES gts_candidate_paper_registration(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    raw_score NUMERIC(10,2),
    adjusted_score NUMERIC(10,2),
    final_score NUMERIC(10,2),

    score_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    absent BOOLEAN NOT NULL DEFAULT FALSE,
    exempted BOOLEAN NOT NULL DEFAULT FALSE,
    missing_mark BOOLEAN NOT NULL DEFAULT FALSE,
    withheld BOOLEAN NOT NULL DEFAULT FALSE,

    absence_reason VARCHAR(500),
    withholding_reason VARCHAR(1000),

    entered_at TIMESTAMPTZ,
    entered_by UUID,

    validated_at TIMESTAMPTZ,
    validated_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    source_type VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    source_reference VARCHAR(160),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_candidate_score
        UNIQUE (tenant_id, mark_sheet_id, student_id),

    CONSTRAINT ck_gts_candidate_score_values
        CHECK (
            (raw_score IS NULL OR raw_score >= 0)
            AND (adjusted_score IS NULL OR adjusted_score >= 0)
            AND (final_score IS NULL OR final_score >= 0)
        ),

    CONSTRAINT ck_gts_candidate_score_special_state
        CHECK (
            (
                absent = FALSE
                AND exempted = FALSE
                AND missing_mark = FALSE
            )
            OR final_score IS NULL
        ),

    CONSTRAINT ck_gts_candidate_score_source
        CHECK (
            source_type IN (
                'MANUAL',
                'BULK_IMPORT',
                'OFFLINE_SYNC',
                'API',
                'SYSTEM',
                'MIGRATION'
            )
        ),

    CONSTRAINT ck_gts_candidate_score_lifecycle
        CHECK (
            score_status IN (
                'DRAFT',
                'ENTERED',
                'VALIDATION_FAILED',
                'VALIDATED',
                'MODERATION',
                'ADJUSTED',
                'APPROVED',
                'LOCKED',
                'WITHHELD',
                'VOIDED'
            )
        ),

    CONSTRAINT ck_gts_candidate_score_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_score_validation_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    rule_code VARCHAR(100) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    description VARCHAR(1200),

    assessment_type_id UUID
        REFERENCES gts_assessment_type(id),

    grading_scheme_id UUID
        REFERENCES gts_grading_scheme(id),

    rule_type VARCHAR(40) NOT NULL,
    rule_configuration JSONB NOT NULL DEFAULT '{}'::jsonb,

    validation_severity VARCHAR(20) NOT NULL DEFAULT 'ERROR',
    blocks_submission BOOLEAN NOT NULL DEFAULT TRUE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_score_validation_rule_code
        UNIQUE (tenant_id, rule_code),

    CONSTRAINT ck_gts_score_validation_rule_type
        CHECK (
            rule_type IN (
                'SCORE_RANGE',
                'MISSING_MARK',
                'DUPLICATE_SCORE',
                'CANDIDATE_ELIGIBILITY',
                'REGISTRATION_REQUIRED',
                'WEIGHT_TOTAL',
                'PASS_MARK',
                'OUTLIER',
                'ABSENCE_CONSISTENCY',
                'GRADE_BOUNDARY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_score_validation_severity
        CHECK (
            validation_severity IN (
                'INFO',
                'WARNING',
                'ERROR',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_score_validation_rule_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_score_validation_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    mark_sheet_id UUID NOT NULL
        REFERENCES gts_mark_sheet(id) ON DELETE CASCADE,

    candidate_score_id UUID
        REFERENCES gts_candidate_score(id) ON DELETE CASCADE,

    validation_rule_id UUID NOT NULL
        REFERENCES gts_score_validation_rule(id),

    validation_status VARCHAR(20) NOT NULL,
    validation_message VARCHAR(1500),

    detected_value JSONB,
    expected_value JSONB,

    detected_at TIMESTAMPTZ NOT NULL,

    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMPTZ,
    resolved_by UUID,
    resolution_notes VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_score_validation_result_status
        CHECK (
            validation_status IN (
                'PASSED',
                'WARNING',
                'FAILED',
                'WAIVED'
            )
        ),

    CONSTRAINT ck_gts_score_validation_resolution
        CHECK (
            resolved = FALSE
            OR resolved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_score_validation_record_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_moderation_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    moderation_reference VARCHAR(100) NOT NULL,

    mark_sheet_id UUID NOT NULL
        REFERENCES gts_mark_sheet(id),

    moderation_type VARCHAR(30) NOT NULL,

    primary_moderator_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    secondary_moderator_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    external_moderator_name VARCHAR(250),
    external_moderator_organization VARCHAR(250),

    sample_size INTEGER,
    sample_percentage NUMERIC(6,2),

    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    moderation_findings VARCHAR(3000),
    moderation_recommendation VARCHAR(2000),

    workflow_instance_id UUID,

    moderation_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_moderation_reference
        UNIQUE (tenant_id, moderation_reference),

    CONSTRAINT ck_gts_moderation_type
        CHECK (
            moderation_type IN (
                'INTERNAL',
                'CROSS_MARKING',
                'SECOND_MARKING',
                'EXTERNAL',
                'STATISTICAL',
                'QUALITY_ASSURANCE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_moderation_sample
        CHECK (
            (sample_size IS NULL OR sample_size >= 0)
            AND (
                sample_percentage IS NULL
                OR (
                    sample_percentage >= 0
                    AND sample_percentage <= 100
                )
            )
        ),

    CONSTRAINT ck_gts_moderation_dates
        CHECK (
            completed_at IS NULL
            OR started_at IS NULL
            OR completed_at >= started_at
        ),

    CONSTRAINT ck_gts_moderation_lifecycle
        CHECK (
            moderation_status IN (
                'PLANNED',
                'ASSIGNED',
                'IN_PROGRESS',
                'CHANGES_REQUIRED',
                'COMPLETED',
                'APPROVED',
                'REJECTED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_moderation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_moderation_score_review (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    moderation_session_id UUID NOT NULL
        REFERENCES gts_moderation_session(id) ON DELETE CASCADE,

    candidate_score_id UUID NOT NULL
        REFERENCES gts_candidate_score(id),

    original_score NUMERIC(10,2),
    moderator_score NUMERIC(10,2),
    recommended_score NUMERIC(10,2),

    variance_amount NUMERIC(10,2),
    variance_percentage NUMERIC(8,2),

    review_outcome VARCHAR(30) NOT NULL DEFAULT 'ACCEPTED',
    review_notes VARCHAR(1500),

    reviewed_at TIMESTAMPTZ NOT NULL,
    reviewed_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_moderation_score_review
        UNIQUE (
            tenant_id,
            moderation_session_id,
            candidate_score_id
        ),

    CONSTRAINT ck_gts_moderation_score_values
        CHECK (
            (original_score IS NULL OR original_score >= 0)
            AND (moderator_score IS NULL OR moderator_score >= 0)
            AND (recommended_score IS NULL OR recommended_score >= 0)
        ),

    CONSTRAINT ck_gts_moderation_review_outcome
        CHECK (
            review_outcome IN (
                'ACCEPTED',
                'ADJUSTMENT_RECOMMENDED',
                'REMARK_REQUIRED',
                'INVALID',
                'WITHHELD'
            )
        ),

    CONSTRAINT ck_gts_moderation_review_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_score_adjustment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    adjustment_reference VARCHAR(100) NOT NULL,

    candidate_score_id UUID NOT NULL
        REFERENCES gts_candidate_score(id),

    moderation_session_id UUID
        REFERENCES gts_moderation_session(id),

    adjustment_type VARCHAR(40) NOT NULL,

    original_score NUMERIC(10,2),
    adjustment_value NUMERIC(10,2),
    adjusted_score NUMERIC(10,2),

    adjustment_reason VARCHAR(1500) NOT NULL,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID NOT NULL,

    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,
    rejection_reason VARCHAR(1000),

    applied_at TIMESTAMPTZ,
    applied_by UUID,

    adjustment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_score_adjustment_reference
        UNIQUE (tenant_id, adjustment_reference),

    CONSTRAINT ck_gts_score_adjustment_type
        CHECK (
            adjustment_type IN (
                'ADD_MARKS',
                'SUBTRACT_MARKS',
                'REPLACE_SCORE',
                'PERCENTAGE_SCALING',
                'NORMALIZATION',
                'MODERATION',
                'REMARK',
                'CORRECTION',
                'SPECIAL_CONSIDERATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_score_adjustment_values
        CHECK (
            (original_score IS NULL OR original_score >= 0)
            AND (adjusted_score IS NULL OR adjusted_score >= 0)
        ),

    CONSTRAINT ck_gts_score_adjustment_approval
        CHECK (
            adjustment_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_score_adjustment_applied
        CHECK (
            adjustment_status <> 'APPLIED'
            OR applied_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_score_adjustment_lifecycle
        CHECK (
            adjustment_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_score_adjustment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_grade_calculation_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    calculation_reference VARCHAR(100) NOT NULL,

    assessment_plan_id UUID NOT NULL
        REFERENCES gts_assessment_plan(id),

    grading_scheme_id UUID NOT NULL
        REFERENCES gts_grading_scheme(id),

    class_offering_id UUID NOT NULL
        REFERENCES gts_class_offering(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    calculation_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD',

    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    initiated_by UUID,
    calculation_parameters JSONB NOT NULL DEFAULT '{}'::jsonb,

    total_students INTEGER NOT NULL DEFAULT 0,
    successful_students INTEGER NOT NULL DEFAULT 0,
    failed_students INTEGER NOT NULL DEFAULT 0,

    workflow_instance_id UUID,

    calculation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_grade_calculation_reference
        UNIQUE (tenant_id, calculation_reference),

    CONSTRAINT ck_gts_grade_calculation_type
        CHECK (
            calculation_type IN (
                'STANDARD',
                'WEIGHTED',
                'COMPETENCY',
                'GPA',
                'BEST_SUBJECTS',
                'AGGREGATE',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_grade_calculation_dates
        CHECK (
            completed_at IS NULL
            OR started_at IS NULL
            OR completed_at >= started_at
        ),

    CONSTRAINT ck_gts_grade_calculation_counts
        CHECK (
            total_students >= 0
            AND successful_students >= 0
            AND failed_students >= 0
            AND successful_students + failed_students <= total_students
        ),

    CONSTRAINT ck_gts_grade_calculation_lifecycle
        CHECK (
            calculation_status IN (
                'PENDING',
                'RUNNING',
                'COMPLETED',
                'PARTIALLY_COMPLETED',
                'FAILED',
                'CANCELLED',
                'SUPERSEDED'
            )
        ),

    CONSTRAINT ck_gts_grade_calculation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_grade_outcome (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    grade_calculation_run_id UUID NOT NULL
        REFERENCES gts_grade_calculation_run(id) ON DELETE CASCADE,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    subject_offering_id UUID NOT NULL
        REFERENCES gts_subject_offering(id),

    weighted_score NUMERIC(10,2),
    final_score NUMERIC(10,2),

    grade_boundary_id UUID
        REFERENCES gts_grade_boundary(id),

    grade_code VARCHAR(40),
    grade_name VARCHAR(120),
    grade_point NUMERIC(8,2),

    passed BOOLEAN,
    competency_level VARCHAR(80),

    outcome_status VARCHAR(30) NOT NULL DEFAULT 'CALCULATED',

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    locked_at TIMESTAMPTZ,
    locked_by UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_grade_outcome
        UNIQUE (
            tenant_id,
            grade_calculation_run_id,
            student_id,
            subject_offering_id
        ),

    CONSTRAINT ck_gts_student_grade_score
        CHECK (
            (weighted_score IS NULL OR weighted_score >= 0)
            AND (final_score IS NULL OR final_score >= 0)
        ),

    CONSTRAINT ck_gts_student_grade_approval
        CHECK (
            outcome_status NOT IN ('APPROVED', 'LOCKED', 'PUBLISHED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_grade_lock
        CHECK (
            outcome_status NOT IN ('LOCKED', 'PUBLISHED')
            OR locked_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_grade_lifecycle
        CHECK (
            outcome_status IN (
                'CALCULATED',
                'VALIDATION_FAILED',
                'VALIDATED',
                'PENDING_APPROVAL',
                'APPROVED',
                'LOCKED',
                'WITHHELD',
                'PUBLISHED',
                'SUPERSEDED'
            )
        ),

    CONSTRAINT ck_gts_student_grade_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_mark_change_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    request_reference VARCHAR(100) NOT NULL,

    candidate_score_id UUID NOT NULL
        REFERENCES gts_candidate_score(id),

    requested_score NUMERIC(10,2),
    request_reason VARCHAR(1500) NOT NULL,

    original_value JSONB NOT NULL,
    requested_value JSONB NOT NULL,

    requested_at TIMESTAMPTZ NOT NULL,
    requested_by UUID NOT NULL,

    workflow_instance_id UUID,
    workflow_task_id UUID,

    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,
    decision_notes VARCHAR(1500),

    applied_at TIMESTAMPTZ,
    applied_by UUID,

    request_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_mark_change_request_reference
        UNIQUE (tenant_id, request_reference),

    CONSTRAINT ck_gts_mark_change_requested_score
        CHECK (
            requested_score IS NULL
            OR requested_score >= 0
        ),

    CONSTRAINT ck_gts_mark_change_applied
        CHECK (
            request_status <> 'APPLIED'
            OR applied_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_mark_change_lifecycle
        CHECK (
            request_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_gts_mark_change_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_marking_grading_history (
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

    CONSTRAINT ck_gts_marking_history_entity
        CHECK (
            entity_type IN (
                'MARK_SHEET',
                'MARK_ENTRY_BATCH',
                'CANDIDATE_SCORE',
                'VALIDATION_RESULT',
                'MODERATION_SESSION',
                'MODERATION_REVIEW',
                'SCORE_ADJUSTMENT',
                'GRADE_CALCULATION',
                'GRADE_OUTCOME',
                'MARK_CHANGE_REQUEST'
            )
        ),

    CONSTRAINT ck_gts_marking_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'OPENED',
                'ENTERED',
                'IMPORTED',
                'VALIDATED',
                'VALIDATION_FAILED',
                'SUBMITTED',
                'RETURNED',
                'MODERATION_STARTED',
                'MODERATION_COMPLETED',
                'ADJUSTMENT_REQUESTED',
                'ADJUSTMENT_APPROVED',
                'ADJUSTMENT_APPLIED',
                'CALCULATION_STARTED',
                'CALCULATION_COMPLETED',
                'APPROVED',
                'LOCKED',
                'UNLOCKED',
                'WITHHELD',
                'PUBLISHED',
                'CHANGE_REQUESTED',
                'CHANGE_APPLIED',
                'VOIDED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_mark_sheet_component
    ON gts_mark_sheet (
        tenant_id,
        assessment_component_id,
        mark_sheet_status
    );

CREATE INDEX ix_gts_mark_sheet_teacher
    ON gts_mark_sheet (
        tenant_id,
        teacher_profile_id,
        submission_deadline,
        mark_sheet_status
    );

CREATE INDEX ix_gts_mark_entry_batch_sheet
    ON gts_mark_entry_batch (
        tenant_id,
        mark_sheet_id,
        batch_status
    );

CREATE INDEX ix_gts_candidate_score_student
    ON gts_candidate_score (
        tenant_id,
        student_id,
        mark_sheet_id,
        score_status
    );

CREATE INDEX ix_gts_candidate_score_sheet
    ON gts_candidate_score (
        tenant_id,
        mark_sheet_id,
        score_status
    );

CREATE INDEX ix_gts_score_validation_result_sheet
    ON gts_score_validation_result (
        tenant_id,
        mark_sheet_id,
        validation_status,
        resolved
    );

CREATE INDEX ix_gts_moderation_session_sheet
    ON gts_moderation_session (
        tenant_id,
        mark_sheet_id,
        moderation_status
    );

CREATE INDEX ix_gts_moderation_score_review_session
    ON gts_moderation_score_review (
        tenant_id,
        moderation_session_id,
        review_outcome
    );

CREATE INDEX ix_gts_score_adjustment_candidate
    ON gts_score_adjustment (
        tenant_id,
        candidate_score_id,
        adjustment_status
    );

CREATE INDEX ix_gts_grade_calculation_plan
    ON gts_grade_calculation_run (
        tenant_id,
        assessment_plan_id,
        calculation_status
    );

CREATE INDEX ix_gts_student_grade_outcome_student
    ON gts_student_grade_outcome (
        tenant_id,
        student_id,
        subject_offering_id,
        outcome_status
    );

CREATE INDEX ix_gts_mark_change_candidate
    ON gts_mark_change_request (
        tenant_id,
        candidate_score_id,
        request_status
    );

CREATE INDEX ix_gts_marking_history_entity
    ON gts_marking_grading_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '042',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
