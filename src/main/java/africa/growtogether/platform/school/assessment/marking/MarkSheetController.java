package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping({
        "/api/v1/school/mark-sheets",
        "/api/school/mark-sheets"
})
public class MarkSheetController {


    private final MarkSheetService service;

    private final EnterpriseIdentityContext identity;


    public MarkSheetController(
            MarkSheetService service,
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
    public MarkSheet create(
            @RequestBody
            CreateMarkSheetCommand command
    ) {

        return service.create(
                identity.requireTenantId(),
                command
        );
    }


    @GetMapping("/{markSheetId}")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public MarkSheet get(
            @PathVariable
            UUID markSheetId
    ) {

        return service.get(
                identity.requireTenantId(),
                markSheetId
        );
    }


    @GetMapping("/reference/{reference}")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.read')"
    )
    public MarkSheet getByReference(
            @PathVariable
            String reference
    ) {

        return service.getByReference(
                identity.requireTenantId(),
                reference
        );
    }


    @PostMapping("/{markSheetId}/open")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public MarkSheet open(
            @PathVariable
            UUID markSheetId
    ) {

        return service.open(
                identity.requireTenantId(),
                markSheetId,
                identity.requireUserId()
        );
    }


    @PostMapping("/{markSheetId}/submit")
    @PreAuthorize(
            "hasAuthority('school.academic.assessment.manage')"
    )
    public MarkSheet submit(
            @PathVariable
            UUID markSheetId
    ) {

        return service.submit(
                identity.requireTenantId(),
                markSheetId,
                identity.requireUserId()
        );
    }
}
