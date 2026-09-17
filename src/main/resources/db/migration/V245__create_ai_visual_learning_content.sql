-- GT AI Visual Learning Content Foundation

CREATE TABLE IF NOT EXISTS ai_visual_learning_content (

    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    tenant_id UUID NOT NULL
        REFERENCES eiam_tenant(id),

    subject_name VARCHAR(200),

    topic VARCHAR(200),

    visual_type VARCHAR(100),

    description VARCHAR(5000),

    generation_status VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0

);


CREATE INDEX IF NOT EXISTS ix_ai_visual_learning_content_tenant
ON ai_visual_learning_content(
    tenant_id
);


CREATE INDEX IF NOT EXISTS ix_ai_visual_learning_content_subject
ON ai_visual_learning_content(
    tenant_id,
    subject_name
);


CREATE INDEX IF NOT EXISTS ix_ai_visual_learning_content_topic
ON ai_visual_learning_content(
    tenant_id,
    topic
);

