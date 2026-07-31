package africa.growtogether.platform.school.lifecycle;


import africa.growtogether.platform.school.lifecycle.audit.SchoolLifecycleAuditEventType;
import africa.growtogether.platform.school.lifecycle.audit.SchoolLifecycleAuditRecorder;
import africa.growtogether.platform.school.onboarding.SchoolOnboardingWorkflowService;
import africa.growtogether.platform.school.onboarding.dto.StartSchoolOnboardingRequest;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
public class SchoolLifecycleService {


    private final SchoolOnboardingWorkflowService onboardingService;

    private final SchoolLifecycleAuditRecorder auditRecorder;


    public SchoolLifecycleService(
            SchoolOnboardingWorkflowService onboardingService,
            SchoolLifecycleAuditRecorder auditRecorder
    ) {

        this.onboardingService = onboardingService;
        this.auditRecorder = auditRecorder;
    }


    public void startSchoolLifecycle(
            UUID tenantId,
            UUID schoolConfigurationId
    ) {

        StartSchoolOnboardingRequest request =
                new StartSchoolOnboardingRequest(
                        schoolConfigurationId
                );


        onboardingService.start(
                tenantId,
                request
        );


        auditRecorder.success(
                SchoolLifecycleAuditEventType.SCHOOL_LIFECYCLE_STARTED.name(),
                schoolConfigurationId.toString(),
                "School lifecycle started",
                Map.of(
                        "schoolConfigurationId",
                        schoolConfigurationId.toString()
                )
        );
    }
}