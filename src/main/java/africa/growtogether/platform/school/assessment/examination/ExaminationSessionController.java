package africa.growtogether.platform.school.assessment.examination;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping({
        "/api/v1/school/examination-sessions",
        "/school/examination-sessions"
})
public class ExaminationSessionController {

    private final ExaminationSessionService service;
    private final EnterpriseIdentityContext identity;


    public ExaminationSessionController(
            ExaminationSessionService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }


    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.create')"
    )
    public ExaminationSession create(
            @Valid @RequestBody
            CreateExaminationSessionCommand command
    ) {
        return service.create(
                identity.requireTenantId(),
                command
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public ExaminationSession get(
            @PathVariable UUID id
    ) {
        return service.get(
                identity.requireTenantId(),
                id
        );
    }


    @GetMapping("/code/{sessionCode}")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public ExaminationSession getByCode(
            @PathVariable String sessionCode
    ) {
        return service.getByCode(
                identity.requireTenantId(),
                sessionCode
        );
    }


    @PostMapping("/{id}/approve")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public ExaminationSession approve(
            @PathVariable UUID id
    ) {
        return service.approve(
                identity.requireTenantId(),
                id,
                identity.requireUserId()
        );
    }


    @PostMapping("/{id}/open-registration")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public ExaminationSession openRegistration(
            @PathVariable UUID id
    ) {
        return service.openRegistration(
                identity.requireTenantId(),
                id
        );
    }


    @PostMapping("/{id}/activate")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public ExaminationSession activate(
            @PathVariable UUID id
    ) {
        return service.activate(
                identity.requireTenantId(),
                id
        );
    }


    @PostMapping("/{id}/complete")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public ExaminationSession complete(
            @PathVariable UUID id
    ) {
        return service.complete(
                identity.requireTenantId(),
                id
        );
    }
}
