package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school")
public class AdmissionApplicationController {

    private final AdmissionApplicationService service;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;

    public AdmissionApplicationController(
            AdmissionApplicationService service,
            EnterpriseIdentityContext identity,
            ApiResponses responses
    ) {

        this.service = service;
        this.identity = identity;
        this.responses = responses;
    }

    @PostMapping("/admissions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('school.admission.application.manage')"
    )
    public ApiResponse<AdmissionHttpDtos.ApplicationView>
    createDraft(
            @Valid
            @RequestBody
            CreateAdmissionApplicationCommand command
    ) {

        AdmissionApplication application =
                service.createDraft(
                        identity.requireTenantId(),
                        command
                );

        return responses.success(
                "GT-SCHOOL-ADMISSION-CORE-001",
                "Admission application draft created.",
                AdmissionHttpDtos.application(
                        application
                )
        );
    }
}
