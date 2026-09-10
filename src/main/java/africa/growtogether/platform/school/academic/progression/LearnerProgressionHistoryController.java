package africa.growtogether.platform.school.academic.progression;


import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/school/progression/history"
)
public class LearnerProgressionHistoryController {


    private final LearnerProgressionHistoryService service;


    public LearnerProgressionHistoryController(
            LearnerProgressionHistoryService service
    ) {

        this.service = service;

    }


    @PostMapping
    public LearnerProgressionHistory create(
            @RequestParam UUID tenantId,
            @RequestParam UUID learnerId,
            @RequestParam UUID promotionDecisionId,
            @RequestParam String progressionType,
            @RequestParam LocalDate effectiveDate
    ) {

        return service.recordProgression(
                tenantId,
                learnerId,
                promotionDecisionId,
                progressionType,
                effectiveDate
        );

    }


    @GetMapping(
            "/learner/{learnerId}"
    )
    public List<LearnerProgressionHistory> findLearnerHistory(
            @RequestParam UUID tenantId,
            @PathVariable UUID learnerId
    ) {

        return service.findLearnerHistory(
                tenantId,
                learnerId
        );

    }


    @GetMapping(
            "/year/{academicYearId}"
    )
    public List<LearnerProgressionHistory> findAcademicYearProgressions(
            @RequestParam UUID tenantId,
            @PathVariable UUID academicYearId
    ) {

        return service.findAcademicYearProgressions(
                tenantId,
                academicYearId
        );

    }


    @GetMapping(
            "/decision/{promotionDecisionId}"
    )
    public List<LearnerProgressionHistory> findByPromotionDecision(
            @RequestParam UUID tenantId,
            @PathVariable UUID promotionDecisionId
    ) {

        return service.findByPromotionDecision(
                tenantId,
                promotionDecisionId
        );

    }

}
