package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school")
public class AdmissionGuardianController {

    private final AdmissionGuardianService service;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;

    public AdmissionGuardianController(
            AdmissionGuardianService service,
            EnterpriseIdentityContext identity,
            ApiResponses responses
    ) {

        this.service = service;
        this.identity = identity;
        this.responses = responses;
    }

    @PostMapping(
            "/admissions/{applicationId}/guardians"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('school.admission.guardian.manage')"
    )
    public ApiResponse<AdmissionHttpDtos.GuardianView>
    create(
            @PathVariable
            UUID applicationId,

            @Valid
            @RequestBody
            CreateAdmissionGuardianCommand command
    ) {

        AdmissionGuardian guardian =
                service.create(
                        identity.requireTenantId(),
                        applicationId,
                        command
                );

        return responses.success(
                "GT-SCHOOL-ADMISSION-GUARDIAN-001",
                "Admission guardian created.",
                AdmissionHttpDtos.guardian(
                        guardian
                )
        );
    }
}
