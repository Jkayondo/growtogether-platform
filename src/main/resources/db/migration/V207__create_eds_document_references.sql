-- EDS document-to-domain reference linkage.
-- Reconstructed from the DocumentReference persistence contract
-- and the existing EDS document foundation.

CREATE TABLE eds_document_references (

    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    document_id UUID NOT NULL
        REFERENCES eds_documents(id),

    reference_type VARCHAR(80) NOT NULL,

    reference_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    created_by VARCHAR(150) NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    updated_by VARCHAR(150) NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT uk_eds_document_reference
        UNIQUE (
            tenant_id,
            document_id,
            reference_type,
            reference_id
        )
);
