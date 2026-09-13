package africa.growtogether.platform.school.academic.assessment;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        "/api/v1/school/academic/learning-outcome/{learningOutcomeId}/assessments"
)
public class AssessmentController {


    private final AssessmentService service;
    private final EnterpriseIdentityContext identity;


    public AssessmentController(
            AssessmentService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.assessment.create')")
    public Assessment create(
            @PathVariable UUID learningOutcomeId,
            @RequestParam UUID tenantId,
            @RequestParam String assessmentCode,
            @RequestParam String assessmentTitle
    ) {

        identity.requireTenant(tenantId);

        return service.create(
                tenantId,
                learningOutcomeId,
                assessmentCode,
                assessmentTitle
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.assessment.read')")
    public List<Assessment> list(
            @PathVariable UUID learningOutcomeId,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return service.findByLearningOutcome(
                tenantId,
                learningOutcomeId
        );
    }


    @GetMapping("/{assessmentCode}")
    @PreAuthorize("hasAuthority('school.academic.assessment.read')")
    public Assessment get(
            @PathVariable UUID learningOutcomeId,
            @PathVariable String assessmentCode,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return service.findByCode(
                tenantId,
                learningOutcomeId,
                assessmentCode
        );
    }


    @PatchMapping("/{assessmentCode}/archive")
    @PreAuthorize("hasAuthority('school.academic.assessment.manage')")
    public Assessment archive(
            @PathVariable UUID learningOutcomeId,
            @PathVariable String assessmentCode,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        Assessment assessment =
                service.findByCode(
                        tenantId,
                        learningOutcomeId,
                        assessmentCode
                );

        return service.archive(
                assessment
        );
    }

}
