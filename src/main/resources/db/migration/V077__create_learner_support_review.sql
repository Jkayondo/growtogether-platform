CREATE TABLE IF NOT EXISTS gts_learner_support_review (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    support_plan_id UUID NOT NULL,

    review_date DATE,

    reviewer_staff_id UUID,

    review_outcome TEXT,

    observations TEXT,

    next_review_date DATE,

    status VARCHAR(50),

    created_at TIMESTAMP,

    created_by VARCHAR(255),

    updated_at TIMESTAMP,

    updated_by VARCHAR(255)

);
