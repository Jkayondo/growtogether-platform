package africa.growtogether.platform.school.reportcard.integration;


import africa.growtogether.platform.school.parent.integration.ParentEngagementIntegrationService;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocument;
import africa.growtogether.platform.school.reportcard.document.ReportCardDocumentService;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ReportCardReleaseWorkflowService {


    private final ReportCardDocumentService documentService;

    private final ParentEngagementIntegrationService parentEngagement;


    public ReportCardReleaseWorkflowService(
            ReportCardDocumentService documentService,
            ParentEngagementIntegrationService parentEngagement
    ) {

        this.documentService = documentService;
        this.parentEngagement = parentEngagement;
    }


    public ReportCardDocument release(
            ReportCardDocument document
    ) {

        return documentService.release(
                document
        );
    }
}
