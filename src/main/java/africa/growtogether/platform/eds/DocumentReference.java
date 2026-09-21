package africa.growtogether.platform.eds;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "eds_document_references",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_eds_document_reference",
                columnNames = {
                        "tenant_id",
                        "document_id",
                        "reference_type",
                        "reference_id"
                }
        )
)
public class DocumentReference
        extends AuditedTenantEntity {


    @Column(
            name = "document_id",
            nullable = false
    )
    private UUID documentId;


    @Column(
            name = "reference_type",
            nullable = false,
            length = 80
    )
    private String referenceType;


    @Column(
            name = "reference_id",
            nullable = false
    )
    private UUID referenceId;


    protected DocumentReference() {
    }


    public DocumentReference(
            UUID tenantId,
            UUID documentId,
            String referenceType,
            UUID referenceId
    ) {

        setTenantId(
                tenantId
        );

        this.documentId =
                documentId;

        this.referenceType =
                requireText(
                        referenceType,
                        "referenceType"
                );

        this.referenceId =
                referenceId;
    }


    private String requireText(
            String value,
            String field
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim()
                .toUpperCase();
    }


    public UUID getDocumentId() {
        return documentId;
    }


    public String getReferenceType() {
        return referenceType;
    }


    public UUID getReferenceId() {
        return referenceId;
    }

}
