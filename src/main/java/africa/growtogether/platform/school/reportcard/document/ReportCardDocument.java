package africa.growtogether.platform.school.reportcard.document;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "report_card_documents",
        indexes = {
                @Index(
                        name = "ix_report_card_document_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class ReportCardDocument
        extends AuditedTenantEntity {


    @Column(
            name = "report_card_id",
            nullable = false
    )
    private UUID reportCardId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "document_status",
            nullable = false,
            length = 30
    )
    private ReportCardDocumentStatus documentStatus;


    @Column(
            name = "document_reference",
            nullable = false,
            length = 100
    )
    private String documentReference;


    @Column(
            name = "generated_at",
            nullable = false
    )
    private Instant generatedAt;


    protected ReportCardDocument() {
    }


    public ReportCardDocument(
            UUID tenantId,
            UUID reportCardId,
            String documentReference
    ) {

        setTenantId(tenantId);

        this.reportCardId = reportCardId;
        this.documentReference = documentReference;
        this.documentStatus =
                ReportCardDocumentStatus.GENERATED;
        this.generatedAt = Instant.now();
    }


    public UUID getReportCardId() {
        return reportCardId;
    }


    public ReportCardDocumentStatus getDocumentStatus() {
        return documentStatus;
    }


    public String getDocumentReference() {
        return documentReference;
    }


    public Instant getGeneratedAt() {
        return generatedAt;
    }


    public void release() {

        this.documentStatus =
                ReportCardDocumentStatus.RELEASED;
    }


    public void approve() {

        this.documentStatus =
                ReportCardDocumentStatus.APPROVED;
    }
}
