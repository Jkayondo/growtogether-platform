package africa.growtogether.platform.school.assessment.examination.candidate;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping({
        "/api/v1/school/examination-candidates",
        "/api/school/examination-candidates"
})
public class ExaminationCandidateController {


    private final ExaminationCandidateService service;
    private final EnterpriseIdentityContext identity;


    public ExaminationCandidateController(
            ExaminationCandidateService service,
            EnterpriseIdentityContext identity
    ) {

        this.service = service;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.create')"
    )
    public ExaminationCandidate register(
            @Valid
            @RequestBody
            CreateExaminationCandidateCommand command
    ) {

        return service.register(
                identity.requireTenantId(),
                command.candidateNumber(),
                command.examinationSessionId(),
                command.studentId(),
                command.studentEnrollmentId()
        );
    }


    @GetMapping("/{candidateNumber}")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public ExaminationCandidate get(
            @PathVariable String candidateNumber
    ) {

        return service.get(
                identity.requireTenantId(),
                candidateNumber
        );
    }

}
