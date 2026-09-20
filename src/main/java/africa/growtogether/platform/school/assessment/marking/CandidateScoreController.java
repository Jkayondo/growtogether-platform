package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping({
        "/api/v1/school/candidate-scores",
        "/api/school/candidate-scores"
})
public class CandidateScoreController {


    private final CandidateScoreService service;

    private final EnterpriseIdentityContext identity;


    public CandidateScoreController(
            CandidateScoreService service,
            EnterpriseIdentityContext identity
    ) {

        this.service = service;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.create')"
    )
    public CandidateScore create(
            @RequestBody
            CreateCandidateScoreCommand command
    ) {

        return service.create(
                identity.requireTenantId(),
                command
        );
    }


    @GetMapping("/{candidateScoreId}")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public CandidateScore get(
            @PathVariable
            UUID candidateScoreId
    ) {

        return service.get(
                identity.requireTenantId(),
                candidateScoreId
        );
    }


    @GetMapping(
            "/mark-sheet/{markSheetId}/student/{studentId}"
    )
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public CandidateScore getByMarkSheetAndStudent(
            @PathVariable
            UUID markSheetId,

            @PathVariable
            UUID studentId
    ) {

        return service.getByMarkSheetAndStudent(
                identity.requireTenantId(),
                markSheetId,
                studentId
        );
    }


    @PostMapping("/{candidateScoreId}/score")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public CandidateScore enterScore(
            @PathVariable
            UUID candidateScoreId,

            @RequestBody
            EnterCandidateScoreCommand command
    ) {

        return service.enterScore(
                identity.requireTenantId(),
                candidateScoreId,
                command.score(),
                identity.requireUserId()
        );
    }


    @PostMapping("/{candidateScoreId}/absent")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public CandidateScore markAbsent(
            @PathVariable
            UUID candidateScoreId
    ) {

        return service.markAbsent(
                identity.requireTenantId(),
                candidateScoreId,
                identity.requireUserId()
        );
    }
}
