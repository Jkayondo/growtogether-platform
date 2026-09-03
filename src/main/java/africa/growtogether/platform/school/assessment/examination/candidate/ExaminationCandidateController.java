package africa.growtogether.platform.school.assessment.examination.candidate;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/school/examination-candidates")
public class ExaminationCandidateController {


    private final ExaminationCandidateService service;


    public ExaminationCandidateController(
            ExaminationCandidateService service
    ) {

        this.service = service;

    }


    @GetMapping("/{candidateNumber}")
    public ExaminationCandidate get(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String candidateNumber
    ) {

        return service.get(
                tenantId,
                candidateNumber
        );

    }

}
