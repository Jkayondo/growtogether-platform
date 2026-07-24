CREATE TABLE gts_behaviour_category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    category_code VARCHAR(80) NOT NULL,
    category_name VARCHAR(200) NOT NULL,
    description VARCHAR(1200),

    behaviour_type VARCHAR(30) NOT NULL,
    default_severity VARCHAR(20),
    safeguarding_related BOOLEAN NOT NULL DEFAULT FALSE,
    parent_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    investigation_required BOOLEAN NOT NULL DEFAULT FALSE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_behaviour_category_code
        UNIQUE (tenant_id, category_code),

    CONSTRAINT ck_gts_behaviour_category_type
        CHECK (
            behaviour_type IN (
                'POSITIVE',
                'CONCERN',
                'MISCONDUCT',
                'SAFEGUARDING',
                'WELFARE',
                'ATTENDANCE',
                'ACADEMIC',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_behaviour_category_severity
        CHECK (
            default_severity IS NULL
            OR default_severity IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_behaviour_category_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_positive_behaviour_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    recognition_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    behaviour_category_id UUID NOT NULL
        REFERENCES gts_behaviour_category(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID
        REFERENCES gts_campus(id),

    recognition_type VARCHAR(40) NOT NULL,
    recognition_title VARCHAR(250) NOT NULL,
    recognition_description VARCHAR(2000),

    points_awarded INTEGER NOT NULL DEFAULT 0,

    occurred_at TIMESTAMPTZ NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    recorded_by UUID,

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    evidence_document_id UUID,

    guardian_notification_required BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_notification_request_id UUID,
    guardian_notified_at TIMESTAMPTZ,

    recognition_status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_positive_behaviour_reference
        UNIQUE (tenant_id, recognition_reference),

    CONSTRAINT ck_gts_positive_behaviour_type
        CHECK (
            recognition_type IN (
                'PRAISE',
                'MERIT',
                'CERTIFICATE',
                'AWARD',
                'LEADERSHIP',
                'SERVICE',
                'ACADEMIC_PROGRESS',
                'ATTENDANCE',
                'SPORTS',
                'CREATIVITY',
                'TEAMWORK',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_positive_behaviour_points
        CHECK (points_awarded >= 0),

    CONSTRAINT ck_gts_positive_behaviour_lifecycle
        CHECK (
            recognition_status IN (
                'DRAFT',
                'RECORDED',
                'APPROVED',
                'PUBLISHED',
                'WITHDRAWN',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_positive_behaviour_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_discipline_incident (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    incident_reference VARCHAR(100) NOT NULL,

    behaviour_category_id UUID NOT NULL
        REFERENCES gts_behaviour_category(id),

    academic_year_id UUID NOT NULL
        REFERENCES gts_academic_year(id),

    academic_term_id UUID
        REFERENCES gts_academic_term(id),

    campus_id UUID NOT NULL
        REFERENCES gts_campus(id),

    incident_title VARCHAR(250) NOT NULL,
    incident_description VARCHAR(4000) NOT NULL,

    incident_date DATE NOT NULL,
    incident_time TIME,
    location_description VARCHAR(300),

    severity VARCHAR(20) NOT NULL,
    incident_source VARCHAR(30) NOT NULL DEFAULT 'STAFF_REPORT',

    confidential BOOLEAN NOT NULL DEFAULT TRUE,
    restricted_access BOOLEAN NOT NULL DEFAULT FALSE,
    safeguarding_related BOOLEAN NOT NULL DEFAULT FALSE,
    police_or_authority_notified BOOLEAN NOT NULL DEFAULT FALSE,

    reported_at TIMESTAMPTZ NOT NULL,
    reported_by UUID,

    reporting_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    workflow_instance_id UUID,

    incident_status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_discipline_incident_reference
        UNIQUE (tenant_id, incident_reference),

    CONSTRAINT ck_gts_discipline_incident_severity
        CHECK (
            severity IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_discipline_incident_source
        CHECK (
            incident_source IN (
                'STAFF_REPORT',
                'STUDENT_REPORT',
                'GUARDIAN_REPORT',
                'ANONYMOUS_REPORT',
                'SYSTEM_ALERT',
                'SECURITY_REPORT',
                'EXTERNAL_AUTHORITY',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_discipline_incident_lifecycle
        CHECK (
            incident_status IN (
                'DRAFT',
                'REPORTED',
                'TRIAGED',
                'UNDER_INVESTIGATION',
                'HEARING_REQUIRED',
                'ACTION_PENDING',
                'ACTION_ACTIVE',
                'RESOLVED',
                'DISMISSED',
                'REFERRED_EXTERNALLY',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_discipline_incident_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_incident_student (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id) ON DELETE CASCADE,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    involvement_role VARCHAR(30) NOT NULL,

    alleged_involvement VARCHAR(2000),
    student_statement VARCHAR(3000),

    immediate_risk_level VARCHAR(20),
    removed_from_activity BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_contact_required BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_incident_student
        UNIQUE (
            tenant_id,
            discipline_incident_id,
            student_id,
            involvement_role
        ),

    CONSTRAINT ck_gts_incident_student_role
        CHECK (
            involvement_role IN (
                'SUBJECT',
                'VICTIM',
                'WITNESS',
                'REPORTER',
                'BYSTANDER',
                'AFFECTED_STUDENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_incident_student_risk
        CHECK (
            immediate_risk_level IS NULL
            OR immediate_risk_level IN (
                'NONE',
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_incident_student_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_incident_witness (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id) ON DELETE CASCADE,

    witness_type VARCHAR(30) NOT NULL,

    student_id UUID
        REFERENCES gts_student(id),

    workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    external_witness_name VARCHAR(250),
    external_witness_contact VARCHAR(200),

    witness_statement VARCHAR(4000),
    statement_recorded_at TIMESTAMPTZ,
    statement_recorded_by UUID,

    confidentiality_requested BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_incident_witness_type
        CHECK (
            witness_type IN (
                'STUDENT',
                'STAFF',
                'GUARDIAN',
                'VISITOR',
                'COMMUNITY_MEMBER',
                'EXTERNAL_AUTHORITY',
                'ANONYMOUS',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_incident_witness_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_incident_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id) ON DELETE CASCADE,

    evidence_type VARCHAR(40) NOT NULL,
    evidence_title VARCHAR(250),
    evidence_description VARCHAR(1500),

    eds_document_id UUID,
    evidence_reference VARCHAR(160),

    collected_at TIMESTAMPTZ NOT NULL,
    collected_by UUID,

    source_description VARCHAR(500),
    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMPTZ,
    verified_by UUID,
    rejection_reason VARCHAR(1000),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_incident_evidence_type
        CHECK (
            evidence_type IN (
                'WRITTEN_STATEMENT',
                'PHOTO',
                'VIDEO',
                'AUDIO',
                'CCTV',
                'MEDICAL_REPORT',
                'SECURITY_REPORT',
                'ATTENDANCE_RECORD',
                'COMMUNICATION_RECORD',
                'PHYSICAL_ITEM',
                'SYSTEM_LOG',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_incident_evidence_verification
        CHECK (
            verification_status IN (
                'PENDING',
                'VERIFIED',
                'REJECTED',
                'INCONCLUSIVE'
            )
        ),

    CONSTRAINT ck_gts_incident_evidence_verified_at
        CHECK (
            verification_status <> 'VERIFIED'
            OR verified_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_incident_evidence_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_safeguarding_action (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    safeguarding_reference VARCHAR(100) NOT NULL,

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    action_type VARCHAR(40) NOT NULL,
    action_description VARCHAR(2500) NOT NULL,

    risk_level VARCHAR(20) NOT NULL,

    immediate_action BOOLEAN NOT NULL DEFAULT TRUE,
    external_referral_required BOOLEAN NOT NULL DEFAULT FALSE,
    external_authority VARCHAR(250),
    external_reference VARCHAR(160),

    guardian_notification_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    restriction_reason VARCHAR(1500),

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    initiated_at TIMESTAMPTZ NOT NULL,
    initiated_by UUID,

    review_due_at TIMESTAMPTZ,
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,

    workflow_instance_id UUID,

    action_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_safeguarding_action_reference
        UNIQUE (tenant_id, safeguarding_reference),

    CONSTRAINT ck_gts_safeguarding_action_type
        CHECK (
            action_type IN (
                'IMMEDIATE_PROTECTION',
                'MEDICAL_SUPPORT',
                'SAFE_LOCATION',
                'STAFF_SUPERVISION',
                'GUARDIAN_CONTACT',
                'CHILD_PROTECTION_REFERRAL',
                'POLICE_REFERRAL',
                'SOCIAL_SERVICES_REFERRAL',
                'COUNSELLING_REFERRAL',
                'RISK_ASSESSMENT',
                'CONTACT_RESTRICTION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_safeguarding_action_risk
        CHECK (
            risk_level IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_safeguarding_action_lifecycle
        CHECK (
            action_status IN (
                'PLANNED',
                'ACTIVE',
                'UNDER_REVIEW',
                'ESCALATED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_safeguarding_action_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_incident_investigation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    investigation_reference VARCHAR(100) NOT NULL,

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id),

    lead_investigator_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    investigation_scope VARCHAR(2500),
    investigation_findings VARCHAR(4000),
    conclusion VARCHAR(2500),
    recommendations VARCHAR(3000),

    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    evidence_sufficient BOOLEAN,
    allegation_substantiated BOOLEAN,

    workflow_instance_id UUID,

    investigation_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_incident_investigation_reference
        UNIQUE (tenant_id, investigation_reference),

    CONSTRAINT ck_gts_incident_investigation_dates
        CHECK (
            completed_at IS NULL
            OR started_at IS NULL
            OR completed_at >= started_at
        ),

    CONSTRAINT ck_gts_incident_investigation_lifecycle
        CHECK (
            investigation_status IN (
                'PLANNED',
                'ASSIGNED',
                'IN_PROGRESS',
                'AWAITING_EVIDENCE',
                'COMPLETED',
                'REFERRED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_incident_investigation_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_disciplinary_hearing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    hearing_reference VARCHAR(100) NOT NULL,

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id),

    investigation_id UUID
        REFERENCES gts_incident_investigation(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    hearing_type VARCHAR(30) NOT NULL,

    scheduled_at TIMESTAMPTZ NOT NULL,
    hearing_location VARCHAR(250),

    chairperson_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_required BOOLEAN NOT NULL DEFAULT TRUE,
    guardian_id UUID
        REFERENCES gts_guardian(id),

    student_representative_name VARCHAR(250),

    hearing_summary VARCHAR(4000),
    findings VARCHAR(3000),
    decision VARCHAR(3000),

    workflow_instance_id UUID,

    completed_at TIMESTAMPTZ,

    hearing_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_disciplinary_hearing_reference
        UNIQUE (tenant_id, hearing_reference),

    CONSTRAINT ck_gts_disciplinary_hearing_type
        CHECK (
            hearing_type IN (
                'INFORMAL_REVIEW',
                'FORMAL_DISCIPLINARY',
                'SAFEGUARDING_REVIEW',
                'APPEAL_HEARING',
                'RESTORATIVE_CONFERENCE',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_disciplinary_hearing_completion
        CHECK (
            hearing_status <> 'COMPLETED'
            OR completed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_disciplinary_hearing_lifecycle
        CHECK (
            hearing_status IN (
                'SCHEDULED',
                'CONFIRMED',
                'IN_PROGRESS',
                'ADJOURNED',
                'COMPLETED',
                'CANCELLED',
                'POSTPONED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_disciplinary_hearing_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_disciplinary_action (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    action_reference VARCHAR(100) NOT NULL,

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id),

    disciplinary_hearing_id UUID
        REFERENCES gts_disciplinary_hearing(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    action_type VARCHAR(40) NOT NULL,
    action_description VARCHAR(2500),

    start_date DATE,
    end_date DATE,

    duration_days INTEGER,

    restorative_action BOOLEAN NOT NULL DEFAULT FALSE,
    academic_access_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    campus_access_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    activity_access_restricted BOOLEAN NOT NULL DEFAULT FALSE,

    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    completed_at TIMESTAMPTZ,
    completed_by UUID,

    action_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_disciplinary_action_reference
        UNIQUE (tenant_id, action_reference),

    CONSTRAINT ck_gts_disciplinary_action_type
        CHECK (
            action_type IN (
                'VERBAL_WARNING',
                'WRITTEN_WARNING',
                'DETENTION',
                'COMMUNITY_SERVICE',
                'RESTORATIVE_CONFERENCE',
                'BEHAVIOUR_CONTRACT',
                'COUNSELLING',
                'PARENT_MEETING',
                'LOSS_OF_PRIVILEGE',
                'INTERNAL_SUSPENSION',
                'EXTERNAL_SUSPENSION',
                'TRANSFER_RECOMMENDATION',
                'EXPULSION_RECOMMENDATION',
                'NO_ACTION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_disciplinary_action_dates
        CHECK (
            end_date IS NULL
            OR start_date IS NULL
            OR end_date >= start_date
        ),

    CONSTRAINT ck_gts_disciplinary_action_duration
        CHECK (
            duration_days IS NULL
            OR duration_days >= 0
        ),

    CONSTRAINT ck_gts_disciplinary_action_approval
        CHECK (
            approval_required = FALSE
            OR action_status NOT IN ('APPROVED', 'ACTIVE', 'COMPLETED')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_disciplinary_action_completion
        CHECK (
            action_status <> 'COMPLETED'
            OR completed_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_disciplinary_action_lifecycle
        CHECK (
            action_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'APPROVED',
                'ACTIVE',
                'COMPLETED',
                'BREACHED',
                'REVOKED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_disciplinary_action_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_guardian_discipline_engagement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    engagement_reference VARCHAR(100) NOT NULL,

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID NOT NULL
        REFERENCES gts_guardian(id),

    engagement_type VARCHAR(40) NOT NULL,

    communication_channel VARCHAR(30),
    scheduled_at TIMESTAMPTZ,
    occurred_at TIMESTAMPTZ,

    discussion_summary VARCHAR(3000),
    agreed_actions VARCHAR(2500),

    guardian_response VARCHAR(2000),
    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_due_at TIMESTAMPTZ,

    notification_request_id UUID,

    engagement_status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_guardian_discipline_engagement_reference
        UNIQUE (tenant_id, engagement_reference),

    CONSTRAINT ck_gts_guardian_discipline_engagement_type
        CHECK (
            engagement_type IN (
                'NOTIFICATION',
                'PHONE_CALL',
                'MEETING',
                'HEARING',
                'HOME_VISIT',
                'RESTORATIVE_CONFERENCE',
                'FOLLOW_UP',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_guardian_discipline_channel
        CHECK (
            communication_channel IS NULL
            OR communication_channel IN (
                'SMS',
                'EMAIL',
                'PHONE',
                'WHATSAPP',
                'IN_APP',
                'LETTER',
                'IN_PERSON',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_guardian_discipline_lifecycle
        CHECK (
            engagement_status IN (
                'PLANNED',
                'NOTIFIED',
                'CONFIRMED',
                'COMPLETED',
                'NO_RESPONSE',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_guardian_discipline_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_welfare_referral (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    referral_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    referral_type VARCHAR(40) NOT NULL,
    referral_reason VARCHAR(3000) NOT NULL,

    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',

    referred_at TIMESTAMPTZ NOT NULL,
    referred_by UUID,

    referred_to_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    external_provider_name VARCHAR(250),
    external_referral_reference VARCHAR(160),

    guardian_consent_required BOOLEAN NOT NULL DEFAULT TRUE,
    guardian_consent_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    guardian_consent_at TIMESTAMPTZ,

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    workflow_instance_id UUID,

    referral_status VARCHAR(30) NOT NULL DEFAULT 'REFERRED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_welfare_referral_reference
        UNIQUE (tenant_id, referral_reference),

    CONSTRAINT ck_gts_welfare_referral_type
        CHECK (
            referral_type IN (
                'COUNSELLING',
                'MENTAL_WELLBEING',
                'MEDICAL',
                'SAFEGUARDING',
                'SOCIAL_SUPPORT',
                'FAMILY_SUPPORT',
                'SPECIAL_NEEDS',
                'ACADEMIC_SUPPORT',
                'BEHAVIOUR_SUPPORT',
                'SUBSTANCE_MISUSE',
                'BULLYING_SUPPORT',
                'EXTERNAL_SPECIALIST',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_welfare_referral_risk
        CHECK (
            risk_level IN (
                'LOW',
                'MODERATE',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT ck_gts_welfare_referral_consent
        CHECK (
            guardian_consent_recorded = FALSE
            OR guardian_consent_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_welfare_referral_lifecycle
        CHECK (
            referral_status IN (
                'DRAFT',
                'REFERRED',
                'ACCEPTED',
                'WAITLISTED',
                'ACTIVE',
                'COMPLETED',
                'DECLINED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_welfare_referral_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_counselling_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    session_reference VARCHAR(100) NOT NULL,

    welfare_referral_id UUID
        REFERENCES gts_welfare_referral(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    counsellor_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    session_type VARCHAR(30) NOT NULL,

    scheduled_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,

    session_summary VARCHAR(3000),
    recommendations VARCHAR(2500),

    risk_identified BOOLEAN NOT NULL DEFAULT FALSE,
    escalation_required BOOLEAN NOT NULL DEFAULT FALSE,

    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_date DATE,

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    session_status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_counselling_session_reference
        UNIQUE (tenant_id, session_reference),

    CONSTRAINT ck_gts_counselling_session_type
        CHECK (
            session_type IN (
                'INDIVIDUAL',
                'GROUP',
                'FAMILY',
                'CRISIS',
                'FOLLOW_UP',
                'ASSESSMENT',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_counselling_session_dates
        CHECK (
            ended_at IS NULL
            OR started_at IS NULL
            OR ended_at >= started_at
        ),

    CONSTRAINT ck_gts_counselling_session_lifecycle
        CHECK (
            session_status IN (
                'SCHEDULED',
                'CONFIRMED',
                'IN_PROGRESS',
                'COMPLETED',
                'MISSED',
                'CANCELLED',
                'REFERRED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_counselling_session_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_learner_support_plan (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    support_plan_reference VARCHAR(100) NOT NULL,

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    student_enrollment_id UUID
        REFERENCES gts_student_enrollment(id),

    welfare_referral_id UUID
        REFERENCES gts_welfare_referral(id),

    discipline_incident_id UUID
        REFERENCES gts_discipline_incident(id),

    plan_type VARCHAR(40) NOT NULL,
    plan_title VARCHAR(250) NOT NULL,
    plan_description VARCHAR(3000),

    goals JSONB NOT NULL DEFAULT '[]'::jsonb,
    interventions JSONB NOT NULL DEFAULT '[]'::jsonb,
    success_criteria JSONB NOT NULL DEFAULT '[]'::jsonb,

    responsible_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    planned_start_date DATE NOT NULL,
    planned_end_date DATE,

    review_frequency VARCHAR(30),
    next_review_date DATE,

    workflow_instance_id UUID,

    approved_at TIMESTAMPTZ,
    approved_by UUID,

    plan_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_learner_support_plan_reference
        UNIQUE (tenant_id, support_plan_reference),

    CONSTRAINT ck_gts_learner_support_plan_type
        CHECK (
            plan_type IN (
                'BEHAVIOUR_SUPPORT',
                'WELFARE_SUPPORT',
                'SAFEGUARDING_PLAN',
                'COUNSELLING_PLAN',
                'ATTENDANCE_SUPPORT',
                'ACADEMIC_SUPPORT',
                'SPECIAL_NEEDS_SUPPORT',
                'REINTEGRATION_PLAN',
                'RESTORATIVE_PLAN',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_learner_support_plan_dates
        CHECK (
            planned_end_date IS NULL
            OR planned_end_date >= planned_start_date
        ),

    CONSTRAINT ck_gts_learner_support_review_frequency
        CHECK (
            review_frequency IS NULL
            OR review_frequency IN (
                'WEEKLY',
                'FORTNIGHTLY',
                'MONTHLY',
                'TERM',
                'CUSTOM'
            )
        ),

    CONSTRAINT ck_gts_learner_support_plan_approval
        CHECK (
            plan_status NOT IN ('APPROVED', 'ACTIVE')
            OR approved_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_learner_support_plan_lifecycle
        CHECK (
            plan_status IN (
                'DRAFT',
                'PENDING_APPROVAL',
                'APPROVED',
                'ACTIVE',
                'UNDER_REVIEW',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_learner_support_plan_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_support_plan_review (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    learner_support_plan_id UUID NOT NULL
        REFERENCES gts_learner_support_plan(id) ON DELETE CASCADE,

    review_date DATE NOT NULL,

    progress_rating VARCHAR(30) NOT NULL,
    progress_summary VARCHAR(2500),

    goal_progress JSONB NOT NULL DEFAULT '[]'::jsonb,

    changes_required BOOLEAN NOT NULL DEFAULT FALSE,
    recommended_changes VARCHAR(2000),

    reviewed_by_workforce_member_id UUID
        REFERENCES ewf_workforce_member(id),

    guardian_participated BOOLEAN NOT NULL DEFAULT FALSE,
    student_participated BOOLEAN NOT NULL DEFAULT FALSE,

    next_review_date DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_gts_support_plan_review_rating
        CHECK (
            progress_rating IN (
                'NO_PROGRESS',
                'LIMITED_PROGRESS',
                'SOME_PROGRESS',
                'GOOD_PROGRESS',
                'GOALS_ACHIEVED',
                'REGRESSION'
            )
        ),

    CONSTRAINT ck_gts_support_plan_review_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_discipline_appeal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES eiam_tenant(id),

    appeal_reference VARCHAR(100) NOT NULL,

    discipline_incident_id UUID NOT NULL
        REFERENCES gts_discipline_incident(id),

    disciplinary_action_id UUID
        REFERENCES gts_disciplinary_action(id),

    student_id UUID NOT NULL
        REFERENCES gts_student(id),

    guardian_id UUID
        REFERENCES gts_guardian(id),

    appeal_type VARCHAR(30) NOT NULL,
    appeal_reason VARCHAR(3000) NOT NULL,

    submitted_at TIMESTAMPTZ NOT NULL,
    submitted_by UUID,

    evidence_document_id UUID,

    workflow_instance_id UUID,

    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID,

    appeal_outcome VARCHAR(30),
    outcome_reason VARCHAR(2500),

    applied_at TIMESTAMPTZ,
    applied_by UUID,

    appeal_status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_gts_discipline_appeal_reference
        UNIQUE (tenant_id, appeal_reference),

    CONSTRAINT ck_gts_discipline_appeal_type
        CHECK (
            appeal_type IN (
                'FINDING',
                'SANCTION',
                'PROCEDURE',
                'NEW_EVIDENCE',
                'SAFEGUARDING_DECISION',
                'OTHER'
            )
        ),

    CONSTRAINT ck_gts_discipline_appeal_outcome
        CHECK (
            appeal_outcome IS NULL
            OR appeal_outcome IN (
                'UPHELD',
                'PARTIALLY_UPHELD',
                'DISMISSED',
                'REHEARING_REQUIRED',
                'ACTION_REDUCED',
                'ACTION_REMOVED'
            )
        ),

    CONSTRAINT ck_gts_discipline_appeal_application
        CHECK (
            appeal_status <> 'APPLIED'
            OR applied_at IS NOT NULL
        ),

    CONSTRAINT ck_gts_discipline_appeal_lifecycle
        CHECK (
            appeal_status IN (
                'DRAFT',
                'SUBMITTED',
                'UNDER_REVIEW',
                'HEARING_SCHEDULED',
                'DECIDED',
                'APPLIED',
                'WITHDRAWN',
                'CANCELLED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_gts_discipline_appeal_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE gts_behaviour_welfare_history (
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

    confidential BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(150) NOT NULL,

    CONSTRAINT ck_gts_behaviour_welfare_history_entity
        CHECK (
            entity_type IN (
                'POSITIVE_BEHAVIOUR',
                'DISCIPLINE_INCIDENT',
                'INCIDENT_STUDENT',
                'INCIDENT_WITNESS',
                'INCIDENT_EVIDENCE',
                'SAFEGUARDING_ACTION',
                'INVESTIGATION',
                'DISCIPLINARY_HEARING',
                'DISCIPLINARY_ACTION',
                'GUARDIAN_ENGAGEMENT',
                'WELFARE_REFERRAL',
                'COUNSELLING_SESSION',
                'SUPPORT_PLAN',
                'SUPPORT_PLAN_REVIEW',
                'DISCIPLINE_APPEAL'
            )
        ),

    CONSTRAINT ck_gts_behaviour_welfare_history_event
        CHECK (
            event_type IN (
                'CREATED',
                'REPORTED',
                'TRIAGED',
                'ASSIGNED',
                'INVESTIGATION_STARTED',
                'INVESTIGATION_COMPLETED',
                'EVIDENCE_ADDED',
                'SAFEGUARDING_ACTION_STARTED',
                'SAFEGUARDING_ACTION_COMPLETED',
                'HEARING_SCHEDULED',
                'HEARING_COMPLETED',
                'ACTION_APPROVED',
                'ACTION_STARTED',
                'ACTION_COMPLETED',
                'GUARDIAN_NOTIFIED',
                'REFERRAL_CREATED',
                'COUNSELLING_COMPLETED',
                'SUPPORT_PLAN_APPROVED',
                'SUPPORT_PLAN_REVIEWED',
                'APPEAL_SUBMITTED',
                'APPEAL_DECIDED',
                'RESOLVED',
                'DISMISSED',
                'CLOSED',
                'ARCHIVED',
                'OTHER'
            )
        )
);

CREATE INDEX ix_gts_behaviour_category_type
    ON gts_behaviour_category (
        tenant_id,
        behaviour_type,
        active
    );

CREATE INDEX ix_gts_positive_behaviour_student
    ON gts_positive_behaviour_record (
        tenant_id,
        student_id,
        occurred_at,
        recognition_status
    );

CREATE INDEX ix_gts_discipline_incident_date
    ON gts_discipline_incident (
        tenant_id,
        incident_date,
        severity,
        incident_status
    );

CREATE INDEX ix_gts_discipline_incident_category
    ON gts_discipline_incident (
        tenant_id,
        behaviour_category_id,
        safeguarding_related,
        incident_status
    );

CREATE INDEX ix_gts_incident_student_student
    ON gts_incident_student (
        tenant_id,
        student_id,
        involvement_role
    );

CREATE INDEX ix_gts_incident_witness_incident
    ON gts_incident_witness (
        tenant_id,
        discipline_incident_id,
        witness_type
    );

CREATE INDEX ix_gts_incident_evidence_incident
    ON gts_incident_evidence (
        tenant_id,
        discipline_incident_id,
        verification_status
    );

CREATE INDEX ix_gts_safeguarding_action_student
    ON gts_safeguarding_action (
        tenant_id,
        student_id,
        risk_level,
        action_status
    );

CREATE INDEX ix_gts_incident_investigation_incident
    ON gts_incident_investigation (
        tenant_id,
        discipline_incident_id,
        investigation_status
    );

CREATE INDEX ix_gts_disciplinary_hearing_student
    ON gts_disciplinary_hearing (
        tenant_id,
        student_id,
        scheduled_at,
        hearing_status
    );

CREATE INDEX ix_gts_disciplinary_action_student
    ON gts_disciplinary_action (
        tenant_id,
        student_id,
        action_status
    );

CREATE INDEX ix_gts_guardian_discipline_student
    ON gts_guardian_discipline_engagement (
        tenant_id,
        student_id,
        guardian_id,
        engagement_status
    );

CREATE INDEX ix_gts_welfare_referral_student
    ON gts_welfare_referral (
        tenant_id,
        student_id,
        risk_level,
        referral_status
    );

CREATE INDEX ix_gts_counselling_session_student
    ON gts_counselling_session (
        tenant_id,
        student_id,
        scheduled_at,
        session_status
    );

CREATE INDEX ix_gts_learner_support_plan_student
    ON gts_learner_support_plan (
        tenant_id,
        student_id,
        plan_type,
        plan_status
    );

CREATE INDEX ix_gts_support_plan_review_plan
    ON gts_support_plan_review (
        tenant_id,
        learner_support_plan_id,
        review_date
    );

CREATE INDEX ix_gts_discipline_appeal_incident
    ON gts_discipline_appeal (
        tenant_id,
        discipline_incident_id,
        appeal_status
    );

CREATE INDEX ix_gts_behaviour_welfare_history_entity
    ON gts_behaviour_welfare_history (
        tenant_id,
        entity_type,
        entity_id,
        effective_at DESC
    );

UPDATE platform_metadata
SET metadata_value = '045',
    updated_at = CURRENT_TIMESTAMP
WHERE metadata_key = 'schema.version';
