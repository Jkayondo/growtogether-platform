CREATE TABLE gts_student_subject_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    result_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    subject_offering_id UUID NOT NULL
        REFERENCES gts_subject_offering(id),

    grade_calculation_run_id UUID
        REFERENCES gts_grade_calculation_run(id),

    grade_outcome_id UUID
        REFERENCES gts_student_grade_outcome(id),

    raw_total_score NUMERIC(10,2),
    weighted_score NUMERIC(10,2),
    final_score NUMERIC(10,2),

    grade_code VARCHAR(40),
    grade_name VARCHAR(120),
    grade_point NUMERIC(8,2),
    competency_level VARCHAR(120),

    passed BOOLEAN,
    subject_position INTEGER,
    subject_candidate_count INTEGER,

    teacher_comment VARCHAR(2000),

    result_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    published_at TIMESTAMPTZ,
    published_by UUID,

    withheld BOOLEAN NOT NULL DEFAULT FALSE,
    withholding_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_subject_result_reference
        UNIQUE (tenant_id, result_reference),

    CONSTRAINT uq_gts_student_subject_result
        UNIQUE (
            tenant_id,
            student_id,
            academic_year_id,
            academic_term_id,
            subject_offering_id
        ),

    CONSTRAINT ck_gts_student_subject_result_scores
        CHECK (
            (raw_total_score IS NULL OR raw_total_score >= 0)
            AND (weighted_score IS NULL OR weighted_score >= 0)
            AND (final_score IS NULL OR final_score >= 0)
        ),

    CONSTRAINT ck_gts_student_subject_result_position
        CHECK (
            (subject_position IS NULL OR subject_position > 0)
            AND (
                subject_candidate_count IS NULL
                OR subject_candidate_count >= 0
            )
            AND (
                subject_position IS NULL
                OR subject_candidate_count IS NULL
                OR subject_position <= subject_candidate_count
            )
        ),

    CONSTRAINT ck_gts_student_subject_result_approval
        CHECK (
            result_status NOT IN ('APPROVED', 'PUBLISHED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_subject_result_publication
        CHECK (
            result_status <> 'PUBLISHED'
            OR published_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_subject_result_lifecycle
        CHECK (
            result_status IN (
                'DRAFT',
                'CALCULATED',
                'VALIDATION_FAILED',
                'VALIDATED',
                'PENDING_APPROVAL',
                'APPROVED',
                'WITHHELD',
                'PUBLISHED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_subject_result_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_term_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    result_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID NOT NULL
        REFERENCES gts_academic_term(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    assessment_plan_id UUID
        REFERENCES gts_assessment_plan(id),

    total_subjects INTEGER NOT NULL DEFAULT 0,
    subjects_passed INTEGER NOT NULL DEFAULT 0,
    subjects_failed INTEGER NOT NULL DEFAULT 0,

    total_score NUMERIC(12,2),
    average_score NUMERIC(8,2),
    grade_point_average NUMERIC(8,3),
    aggregate_score NUMERIC(10,2),

    overall_grade_code VARCHAR(40),
    overall_grade_name VARCHAR(120),
    overall_competency_level VARCHAR(120),

    class_position INTEGER,
    stream_position INTEGER,
    cohort_position INTEGER,

    class_candidate_count INTEGER,
    stream_candidate_count INTEGER,
    cohort_candidate_count INTEGER,

    attendance_percentage NUMERIC(6,2),

    result_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    published_at TIMESTAMPTZ,
    published_by UUID,

    withheld BOOLEAN NOT NULL DEFAULT FALSE,
    withholding_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_term_result_reference
        UNIQUE (tenant_id, result_reference),

    CONSTRAINT uq_gts_student_term_result
        UNIQUE (
            tenant_id,
            student_id,
            academic_year_id,
            academic_term_id
        ),

    CONSTRAINT ck_gts_student_term_result_counts
        CHECK (
            total_subjects >= 0
            AND subjects_passed >= 0
            AND subjects_failed >= 0
            AND subjects_passed + subjects_failed <= total_subjects
        ),

    CONSTRAINT ck_gts_student_term_result_scores
        CHECK (
            (total_score IS NULL OR total_score >= 0)
            AND (
                average_score IS NULL
                OR (average_score >= 0 AND average_score <= 100)
            )
            AND (
                grade_point_average IS NULL
                OR grade_point_average >= 0
            )
            AND (
                aggregate_score IS NULL
                OR aggregate_score >= 0
            )
        ),

    CONSTRAINT ck_gts_student_term_result_attendance
        CHECK (
            attendance_percentage IS NULL
            OR (
                attendance_percentage >= 0
                AND attendance_percentage <= 100
            )
        ),

    CONSTRAINT ck_gts_student_term_result_positions
        CHECK (
            (class_position IS NULL OR class_position > 0)
            AND (stream_position IS NULL OR stream_position > 0)
            AND (cohort_position IS NULL OR cohort_position > 0)
            AND (
                class_candidate_count IS NULL
                OR class_candidate_count >= 0
            )
            AND (
                stream_candidate_count IS NULL
                OR stream_candidate_count >= 0
            )
            AND (
                cohort_candidate_count IS NULL
                OR cohort_candidate_count >= 0
            )
        ),

    CONSTRAINT ck_gts_student_term_result_approval
        CHECK (
            result_status NOT IN ('APPROVED', 'PUBLISHED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_term_result_publication
        CHECK (
            result_status <> 'PUBLISHED'
            OR published_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_term_result_lifecycle
        CHECK (
            result_status IN (
                'DRAFT',
                'CALCULATED',
                'VALIDATION_FAILED',
                'VALIDATED',
                'PENDING_APPROVAL',
                'APPROVED',
                'WITHHELD',
                'PUBLISHED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_term_result_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_progression_decision (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    decision_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    student_term_result_id UUID
        REFERENCES gts_student_term_result(id),

    progression_rule_id UUID
        REFERENCES gts_progression_rule(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    from_class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    to_class_grade_id UUID
        REFERENCES gts_class_grade(id),

    decision_type VARCHAR(40) NOT NULL,
    decision_reason VARCHAR(2000),

    automatic_decision BOOLEAN NOT NULL DEFAULT FALSE,

    workflow_instance_id UUID,

    decided_at TIMESTAMPTZ,
    decided_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    effective_date DATE,

    decision_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_progression_decision_reference
        UNIQUE (tenant_id, decision_reference),

    CONSTRAINT uq_gts_progression_decision_student_year
        UNIQUE (tenant_id, student_id, academic_year_id),

    CONSTRAINT ck_gts_progression_decision_type
        CHECK (
            decision_type IN (
                'PROMOTED',
                'PROGRESSED',
                'REPEAT_CLASS',
                'PROBATION',
                'GRADUATED',
                'COMPLETED',
                'TRANSFERRED',
                'WITHDRAWN',
                'PENDING_REVIEW',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_progression_decision_approval
        CHECK (
            decision_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_progression_decision_lifecycle
        CHECK (
            decision_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'APPLIED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_progression_decision_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_report_card_template (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(250) NOT NULL,
    description VARCHAR(1500),

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    education_level_id UUID
        REFERENCES gts_education_level(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    template_type VARCHAR(40) NOT NULL DEFAULT 'TERM_REPORT',

    template_configuration JSONB NOT NULL DEFAULT '{}'::jsonb,

    eds_template_document_id UUID,

    includes_subject_positions BOOLEAN NOT NULL DEFAULT FALSE,
    includes_class_position BOOLEAN NOT NULL DEFAULT FALSE,
    includes_attendance BOOLEAN NOT NULL DEFAULT TRUE,
    includes_behaviour BOOLEAN NOT NULL DEFAULT FALSE,
    includes_competencies BOOLEAN NOT NULL DEFAULT FALSE,
    includes_teacher_comments BOOLEAN NOT NULL DEFAULT TRUE,
    includes_headteacher_comment BOOLEAN NOT NULL DEFAULT TRUE,
    includes_parent_acknowledgement BOOLEAN NOT NULL DEFAULT TRUE,

    effective_from DATE,
    effective_to DATE,

    template_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_report_card_template_code
        UNIQUE (tenant_id, template_code),

    CONSTRAINT ck_gts_report_card_template_type
        CHECK (
            template_type IN (
                'TERM_REPORT',
                'MID_TERM_REPORT',
                'ANNUAL_REPORT',
                'PROGRESS_REPORT',
                'COMPETENCY_REPORT',
                'TRANSCRIPT',
                'LEAVING_REPORT',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_report_card_template_dates
        CHECK (
            effective_to IS NULL
            OR effective_from IS NULL
            OR effective_to >= effective_from
        ),

    CONSTRAINT ck_gts_report_card_template_lifecycle
        CHECK (
            template_status IN (
                'DRAFT',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'SUPERSEDED',
                'RETIRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_report_card_template_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_report_card (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    report_card_reference VARCHAR(100) NOT NULL,

    report_card_template_id UUID NOT NULL
        REFERENCES gts_report_card_template(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    student_term_result_id UUID
        REFERENCES gts_student_term_result(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    class_grade_id UUID NOT NULL
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    report_title VARCHAR(250) NOT NULL,

    generated_at TIMESTAMPTZ,
    generated_by UUID,

    eds_report_document_id UUID,

    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    signed_at TIMESTAMPTZ,
    signed_by UUID,

    published_at TIMESTAMPTZ,
    published_by UUID,

    publication_window_id UUID
        REFERENCES gts_result_publication_window(id),

    report_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_report_card_reference
        UNIQUE (tenant_id, report_card_reference),

    CONSTRAINT uq_gts_report_card_period
        UNIQUE (
            tenant_id,
            student_id,
            academic_year_id,
            academic_term_id,
            report_card_template_id
        ),

    CONSTRAINT ck_gts_report_card_generation
        CHECK (
            report_status NOT IN (
                'GENERATED',
                'PENDING_APPROVAL',
                'APPROVED',
                'SIGNED',
                'PUBLISHED'
            )
            OR generated_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_report_card_approval
        CHECK (
            report_status NOT IN ('APPROVED', 'SIGNED', 'PUBLISHED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_report_card_signature
        CHECK (
            report_status NOT IN ('SIGNED', 'PUBLISHED')
            OR signed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_report_card_publication
        CHECK (
            report_status <> 'PUBLISHED'
            OR published_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_report_card_lifecycle
        CHECK (
            report_status IN (
                'DRAFT',
                'GENERATING',
                'GENERATED',
                'GENERATION_FAILED',
                'PENDING_APPROVAL',
                'APPROVED',
                'SIGNED',
                'PUBLISHED',
                'WITHDRAWN',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_report_card_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_report_card_comment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    report_card_id UUID NOT NULL
        REFERENCES gts_report_card(id) ON DELETE CASCADE,

    comment_type VARCHAR(40) NOT NULL,

    subject_offering_id UUID
        REFERENCES gts_subject_offering(id),

    author_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    comment_text VARCHAR(3000) NOT NULL,

    confidential BOOLEAN NOT NULL DEFAULT FALSE,

    submitted_at TIMESTAMPTZ NOT NULL,
    submitted_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    comment_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_report_card_comment_type
        CHECK (
            comment_type IN (
                'SUBJECT_TEACHER',
                'CLASS_TEACHER',
                'HEAD_OF_DEPARTMENT',
                'DIRECTOR_OF_STUDIES',
                'DEPUTY_HEADTEACHER',
                'HEADTEACHER',
                'COUNSELLOR',
                'PARENT',
                'STUDENT_REFLECTION',
                'GENERAL'
            )
        ),

    CONSTRAINT ck_gts_report_card_comment_approval
        CHECK (
            comment_status <> 'APPROVED'
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_report_card_comment_lifecycle
        CHECK (
            comment_status IN (
                'DRAFT',
                'SUBMITTED',
                'UNDER_REVIEW',
                'APPROVED',
                'REJECTED',
                'PUBLISHED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_report_card_comment_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_report_card_acknowledgement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    report_card_id UUID NOT NULL
        REFERENCES gts_report_card(id) ON DELETE CASCADE,

    guardian_id UUID
        REFERENCES gts_guardian(id),

    student_id UUID
        REFERENCES gts_student(id),

    acknowledgement_type VARCHAR(30) NOT NULL,

    acknowledged_at TIMESTAMPTZ NOT NULL,
    acknowledgement_channel VARCHAR(30) NOT NULL,

    acknowledgement_comment VARCHAR(1500),

    source_ip VARCHAR(80),
    device_reference VARCHAR(200),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_report_card_acknowledgement
        UNIQUE (
            tenant_id,
            report_card_id,
            guardian_id,
            student_id,
            acknowledgement_type
        ),

    CONSTRAINT ck_gts_report_card_acknowledgement_type
        CHECK (
            acknowledgement_type IN (
                'VIEWED',
                'RECEIVED',
                'ACKNOWLEDGED',
                'COMMENTED',
                'DISPUTED'
            )
        ),

    CONSTRAINT ck_gts_report_card_acknowledgement_channel
        CHECK (
            acknowledgement_channel IN (
                'WEB',
                'MOBILE',
                'IN_APP',
                'EMAIL',
                'SMS',
                'PRINTED_COPY',
                'OFFICE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_report_card_acknowledgement_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_academic_transcript (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    transcript_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    transcript_type VARCHAR(40) NOT NULL DEFAULT 'ACADEMIC_TRANSCRIPT',

    academic_year_from_id UUID
        REFERENCES gts_academic_year(id),

    academic_year_to_id UUID
        REFERENCES gts_academic_year(id),

    cumulative_average NUMERIC(8,2),
    cumulative_grade_point_average NUMERIC(8,3),
    cumulative_aggregate NUMERIC(10,2),

    final_classification VARCHAR(160),

    generated_at TIMESTAMPTZ,
    generated_by UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    signed_at TIMESTAMPTZ,
    signed_by UUID,

    eds_transcript_document_id UUID,

    verification_code VARCHAR(160),
    verification_hash VARCHAR(255),

    transcript_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_transcript_reference
        UNIQUE (tenant_id, transcript_reference),

    CONSTRAINT uq_gts_academic_transcript_verification
        UNIQUE (tenant_id, verification_code),

    CONSTRAINT ck_gts_academic_transcript_type
        CHECK (
            transcript_type IN (
                'ACADEMIC_TRANSCRIPT',
                'PARTIAL_TRANSCRIPT',
                'FINAL_TRANSCRIPT',
                'TRANSFER_TRANSCRIPT',
                'GRADUATION_TRANSCRIPT',
                'STATEMENT_OF_RESULTS'
            )
        ),

    CONSTRAINT ck_gts_academic_transcript_scores
        CHECK (
            (
                cumulative_average IS NULL
                OR (
                    cumulative_average >= 0
                    AND cumulative_average <= 100
                )
            )
            AND (
                cumulative_grade_point_average IS NULL
                OR cumulative_grade_point_average >= 0
            )
            AND (
                cumulative_aggregate IS NULL
                OR cumulative_aggregate >= 0
            )
        ),

    CONSTRAINT ck_gts_academic_transcript_approval
        CHECK (
            transcript_status NOT IN ('APPROVED', 'SIGNED', 'ISSUED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_academic_transcript_signature
        CHECK (
            transcript_status NOT IN ('SIGNED', 'ISSUED')
            OR signed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_academic_transcript_lifecycle
        CHECK (
            transcript_status IN (
                'DRAFT',
                'GENERATING',
                'GENERATED',
                'PENDING_APPROVAL',
                'APPROVED',
                'SIGNED',
                'ISSUED',
                'REVOKED',
                'SUPERSEDED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_academic_transcript_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_academic_performance_snapshot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    snapshot_reference VARCHAR(100) NOT NULL,

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID
        REFERENCES gts_campus(id),

    academic_programme_id UUID
        REFERENCES gts_academic_programme(id),

    class_grade_id UUID
        REFERENCES gts_class_grade(id),

    stream_id UUID
        REFERENCES gts_stream(id),

    subject_offering_id UUID
        REFERENCES gts_subject_offering(id),

    snapshot_type VARCHAR(40) NOT NULL,

    student_count INTEGER NOT NULL DEFAULT 0,
    assessed_student_count INTEGER NOT NULL DEFAULT 0,
    passed_student_count INTEGER NOT NULL DEFAULT 0,
    failed_student_count INTEGER NOT NULL DEFAULT 0,

    average_score NUMERIC(8,2),
    median_score NUMERIC(8,2),
    highest_score NUMERIC(8,2),
    lowest_score NUMERIC(8,2),
    pass_percentage NUMERIC(6,2),

    grade_distribution JSONB NOT NULL DEFAULT '{}'::jsonb,
    competency_distribution JSONB NOT NULL DEFAULT '{}'::jsonb,

    calculated_at TIMESTAMPTZ NOT NULL,
    calculation_reference VARCHAR(160),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_performance_snapshot_reference
        UNIQUE (tenant_id, snapshot_reference),

    CONSTRAINT ck_gts_academic_performance_snapshot_type
        CHECK (
            snapshot_type IN (
                'STUDENT',
                'SUBJECT',
                'CLASS',
                'STREAM',
                'PROGRAMME',
                'CAMPUS',
                'TERM',
                'ACADEMIC_YEAR',
                'COHORT',
                'TEACHER',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_academic_performance_counts
        CHECK (
            student_count >= 0
            AND assessed_student_count >= 0
            AND passed_student_count >= 0
            AND failed_student_count >= 0
            AND assessed_student_count <= student_count
            AND passed_student_count + failed_student_count <= assessed_student_count
        ),

    CONSTRAINT ck_gts_academic_performance_scores
        CHECK (
            (
                average_score IS NULL
                OR (average_score >= 0 AND average_score <= 100)
            )
            AND (
                median_score IS NULL
                OR (median_score >= 0 AND median_score <= 100)
            )
            AND (
                highest_score IS NULL
                OR highest_score >= 0
            )
            AND (
                lowest_score IS NULL
                OR lowest_score >= 0
            )
            AND (
                pass_percentage IS NULL
                OR (pass_percentage >= 0 AND pass_percentage <= 100)
            )
        ),

    CONSTRAINT ck_gts_academic_performance_snapshot_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_student_academic_risk_indicator (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    indicator_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID NOT NULL
        REFERENCES gts_student_enrollment(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    subject_offering_id UUID
        REFERENCES gts_subject_offering(id),

    risk_type VARCHAR(40) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,

    observed_value NUMERIC(12,4),
    threshold_value NUMERIC(12,4),

    evidence_summary JSONB NOT NULL DEFAULT '{}'::jsonb,

    detected_at TIMESTAMPTZ NOT NULL,
    detected_by VARCHAR(30) NOT NULL DEFAULT 'RULE_ENGINE',

    intervention_required BOOLEAN NOT NULL DEFAULT TRUE,
    workflow_instance_id UUID,

    acknowledged_at TIMESTAMPTZ,
    acknowledged_by UUID,

    resolved_at TIMESTAMPTZ,
    resolved_by UUID,
    resolution_notes VARCHAR(1500),

    indicator_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_student_academic_risk_reference
        UNIQUE (tenant_id, indicator_reference),

    CONSTRAINT ck_gts_student_academic_risk_type
        CHECK (
            risk_type IN (
                'LOW_AVERAGE',
                'SUBJECT_FAILURE',
                'DECLINING_PERFORMANCE',
                'LOW_ATTENDANCE',
                'MISSING_ASSESSMENTS',
                'REPEATED_CLASS',
                'PROGRESSION_RISK',
                'GRADUATION_RISK',
                'COMPETENCY_GAP',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_academic_risk_level
        CHECK (
            risk_level IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_student_academic_risk_detector
        CHECK (
            detected_by IN (
                'RULE_ENGINE',
                'ANALYTICS',
                'AI_ASSISTED',
                'TEACHER',
                'ADMINISTRATOR',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_student_academic_risk_resolution
        CHECK (
            indicator_status <> 'RESOLVED'
            OR resolved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_student_academic_risk_lifecycle
        CHECK (
            indicator_status IN (
                'OPEN',
                'ACKNOWLEDGED',
                'UNDER_REVIEW',
                'INTERVENTION_ACTIVE',
                'RESOLVED',
                'DISMISSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_student_academic_risk_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_academic_intervention (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    intervention_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    risk_indicator_id UUID
        REFERENCES gts_student_academic_risk_indicator(id),

    intervention_type VARCHAR(40) NOT NULL,
    intervention_description VARCHAR(2000) NOT NULL,

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    planned_start_date DATE,
    planned_end_date DATE,

    actual_start_date DATE,
    actual_end_date DATE,

    success_criteria JSONB NOT NULL DEFAULT '{}'::jsonb,
    outcome_summary VARCHAR(2000),

    workflow_instance_id UUID,

    intervention_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_academic_intervention_reference
        UNIQUE (tenant_id, intervention_reference),

    CONSTRAINT ck_gts_academic_intervention_type
        CHECK (
            intervention_type IN (
                'REMEDIAL_LESSONS',
                'TUTORING',
                'COUNSELLING',
                'PARENT_MEETING',
                'LEARNING_SUPPORT',
                'SPECIAL_NEEDS_SUPPORT',
                'ATTENDANCE_SUPPORT',
                'MENTORING',
                'STUDY_PLAN',
                'SUBJECT_CHANGE',
                'ASSESSMENT_ACCOMMODATION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_academic_intervention_planned_dates
        CHECK (
            planned_end_date IS NULL
            OR planned_start_date IS NULL
            OR planned_end_date >= planned_start_date
        ),

    CONSTRAINT ck_gts_academic_intervention_actual_dates
        CHECK (
            actual_end_date IS NULL
            OR actual_start_date IS NULL
            OR actual_end_date >= actual_start_date
        ),

    CONSTRAINT ck_gts_academic_intervention_lifecycle
        CHECK (
            intervention_status IN (
                'PLANNED',
                'PENDING_APPROVAL',
                'APPROVED',
                'ACTIVE',
                'PAUSED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_academic_intervention_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_result_report_history (
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

    CONSTRAINT ck_gts_result_report_history_entity
        CHECK (
            entity_type IN (
                'SUBJECT_RESULT',
                'TERM_RESULT',
                'PROGRESSION_DECISION',
                'REPORT_CARD_TEMPLATE',
                'REPORT_CARD',
                'REPORT_CARD_COMMENT',
                'REPORT_ACKNOWLEDGEMENT',
                'TRANSCRIPT',
                'PERFORMANCE_SNAPSHOT',
                'RISK_INDICATOR',
                'INTERVENTION'
            )
        ),

    CONSTRAINT ck_gts_result_report_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'CALCULATED',
                'VALIDATED',
                'SUBMITTED',
                'APPROVED',
                'REJECTED',
                'WITHHELD',
                'GENERATED',
                'GENERATION_FAILED',
                'SIGNED',
                'PUBLISHED',
                'VIEWED',
                'ACKNOWLEDGED',
                'DISPUTED',
                'ISSUED',
                'REVOKED',
                'RISK_DETECTED',
                'INTERVENTION_STARTED',
                'INTERVENTION_COMPLETED',
                'SUPERSEDED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_student_subject_result_student
    ON gts_student_subject_result (
        tenant_id,
        student_id,
        academic_year_id,
        academic_term_id,
        result_status
    );

CREATE INDEX ix_gts_student_subject_result_subject
    ON gts_student_subject_result (
        tenant_id,
        subject_offering_id,
        result_status
    );

CREATE INDEX ix_gts_student_term_result_student
    ON gts_student_term_result (
        tenant_id,
        student_id,
        academic_year_id,
        academic_term_id
    );

CREATE INDEX ix_gts_student_term_result_class
    ON gts_student_term_result (
        tenant_id,
        class_grade_id,
        stream_id,
        result_status
    );

CREATE INDEX ix_gts_progression_decision_student
    ON gts_progression_decision (
        tenant_id,
        student_id,
        academic_year_id,
        decision_status
    );

CREATE INDEX ix_gts_report_card_template_scope
    ON gts_report_card_template (
        tenant_id,
        academic_programme_id,
        education_level_id,
        class_grade_id,
        template_status
    );

CREATE INDEX ix_gts_report_card_student
    ON gts_report_card (
        tenant_id,
        student_id,
        academic_year_id,
        academic_term_id,
        report_status
    );

CREATE INDEX ix_gts_report_card_comment_report
    ON gts_report_card_comment (
        tenant_id,
        report_card_id,
        comment_type,
        comment_status
    );

CREATE INDEX ix_gts_report_card_acknowledgement_report
    ON gts_report_card_acknowledgement (
        tenant_id,
        report_card_id,
        acknowledgement_type
    );

CREATE INDEX ix_gts_academic_transcript_student
    ON gts_academic_transcript (
        tenant_id,
        student_id,
        transcript_status
    );

CREATE INDEX ix_gts_performance_snapshot_scope
    ON gts_academic_performance_snapshot (
        tenant_id,
        academic_year_id,
        academic_term_id,
        class_grade_id,
        stream_id,
        snapshot_type
    );

CREATE INDEX ix_gts_academic_risk_student
    ON gts_student_academic_risk_indicator (
        tenant_id,
        student_id,
        risk_level,
        indicator_status
    );

CREATE INDEX ix_gts_academic_intervention_student
    ON gts_academic_intervention (
        tenant_id,
        student_id,
        intervention_status
    );

CREATE INDEX ix_gts_result_report_history_entity
    ON gts_result_report_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '043',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
