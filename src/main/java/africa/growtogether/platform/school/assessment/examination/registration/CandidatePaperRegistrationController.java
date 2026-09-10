package africa.growtogether.platform.school.assessment.examination.registration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping({
        "/api/v1/school/candidate-paper-registrations",
        "/api/school/candidate-paper-registrations"
})
public class CandidatePaperRegistrationController {


    private final CandidatePaperRegistrationService service;
    private final EnterpriseIdentityContext identity;


    public CandidatePaperRegistrationController(
            CandidatePaperRegistrationService service,
            EnterpriseIdentityContext identity
    ) {

        this.service =
                service;

        this.identity =
                identity;
    }


    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.create')"
    )
    public CandidatePaperRegistration register(
            @RequestBody
            CreateCandidatePaperRegistrationCommand command
    ) {

        return service.register(
                identity.requireTenantId(),
                identity.requireUserId(),
                command
        );
    }


    @GetMapping
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public CandidatePaperRegistration get(
            @RequestParam UUID examinationCandidateId,
            @RequestParam UUID assessmentPaperId
    ) {

        return service.get(
                identity.requireTenantId(),
                examinationCandidateId,
                assessmentPaperId
        );
    }


    @PostMapping("/verify")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public CandidatePaperRegistration verify(
            @RequestParam UUID examinationCandidateId,
            @RequestParam UUID assessmentPaperId
    ) {

        return service.verify(
                identity.requireTenantId(),
                examinationCandidateId,
                assessmentPaperId
        );
    }

}
