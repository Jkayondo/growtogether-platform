package africa.growtogether.platform.school.reportcard.integration;


import africa.growtogether.platform.school.parent.integration.ParentEngagementIntegrationService;
import africa.growtogether.platform.school.reportcard.audit.ReportCardAuditEventType;
import africa.growtogether.platform.school.reportcard.audit.ReportCardAuditRecorder;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocument;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocumentRepository;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocumentService;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
public class ReportCardReleaseWorkflowService {


    private final ReportCardDocumentService documentService;

    private final ParentEngagementIntegrationService parentEngagement;

    private final ReportCardDocumentRepository repository;

    private final ReportCardAuditRecorder auditRecorder;


    public ReportCardReleaseWorkflowService(
            ReportCardDocumentService documentService,
            ParentEngagementIntegrationService parentEngagement,
            ReportCardDocumentRepository repository,
            ReportCardAuditRecorder auditRecorder
    ) {

        this.documentService = documentService;
        this.parentEngagement = parentEngagement;
        this.repository = repository;
        this.auditRecorder = auditRecorder;
    }


    public ReportCardDocument release(
            ReportCardDocument document
    ) {

        ReportCardDocument released =
                documentService.release(
                        document
                );


        auditRecorder.success(
                ReportCardAuditEventType.REPORT_CARD_RELEASED.name(),
                released.getId().toString(),
                "Report card released",
                Map.of(
                        "reportCardDocumentId",
                        released.getId().toString()
                )
        );


        return released;
    }


    public ReportCardDocument release(
            UUID documentId
    ) {

        ReportCardDocument document =
                repository.findById(documentId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Report card document not found"
                                )
                        );


        return release(document);
    }
}
