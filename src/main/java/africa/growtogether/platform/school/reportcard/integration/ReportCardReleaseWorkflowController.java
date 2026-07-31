package africa.growtogether.platform.school.reportcard.integration;


import africa.growtogether.platform.school.reportcard.document.ReportCardDocument;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/reportcards")
public class ReportCardReleaseWorkflowController {


    private final ReportCardReleaseWorkflowService service;


    public ReportCardReleaseWorkflowController(
            ReportCardReleaseWorkflowService service
    ) {

        this.service = service;
    }


    @PostMapping("/{documentId}/release")
    public ResponseEntity<ReportCardDocument> release(
            @PathVariable UUID documentId
    ) {

        return ResponseEntity.ok(
                service.release(documentId)
        );
    }
}
