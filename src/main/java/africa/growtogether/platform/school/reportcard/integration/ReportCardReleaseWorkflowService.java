package africa.growtogether.platform.school.reportcard.integration;


import africa.growtogether.platform.school.reportcard.document.ReportCardDocumentRepository;
import africa.growtogether.platform.school.parent.integration.ParentEngagementIntegrationService;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocument;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocumentService;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ReportCardReleaseWorkflowService {


    private final ReportCardDocumentService documentService;

    private final ParentEngagementIntegrationService parentEngagement;

    private final ReportCardDocumentRepository repository;


    public ReportCardReleaseWorkflowService(
            ReportCardDocumentService documentService,
            ParentEngagementIntegrationService parentEngagement,
            ReportCardDocumentRepository repository
    ) {

        this.documentService = documentService;
        this.parentEngagement = parentEngagement;
        this.repository = repository;
    }


    public ReportCardDocument release(
            ReportCardDocument document
    ) {

        return documentService.release(
                document
        );
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


       return documentService.release(
               document
    );
}
}
