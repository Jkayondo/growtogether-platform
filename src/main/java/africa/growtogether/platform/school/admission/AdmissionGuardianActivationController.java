package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school")
public class AdmissionGuardianActivationController {

    private final AdmissionGuardianActivationNotificationService service;
    private final ApiResponses responses;

    public AdmissionGuardianActivationController(
            AdmissionGuardianActivationNotificationService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping(
            "/admission-guardians/{admissionGuardianId}"
                    + "/parent-activation/notifications"
    )
    @PreAuthorize(
            "hasAuthority('school.admission.parent-activation.manage')"
    )
    public ApiResponse<AdmissionGuardianActivationNotificationResult>
    provisionAndNotify(
            @PathVariable UUID admissionGuardianId
    ) {
        return responses.success(
                "GT-SCHOOL-ADMISSION-PARENT-ACT-001",
                "Secure parent account activation notification created.",
                service.provisionAndNotify(
                        admissionGuardianId
                )
        );
    }
}
