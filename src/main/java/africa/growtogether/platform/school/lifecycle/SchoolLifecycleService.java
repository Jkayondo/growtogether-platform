package africa.growtogether.platform.school.lifecycle;


import africa.growtogether.platform.school.onboarding.SchoolOnboardingWorkflowService;
import africa.growtogether.platform.school.onboarding.dto.StartSchoolOnboardingRequest;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class SchoolLifecycleService {


    private final SchoolOnboardingWorkflowService onboardingService;


    public SchoolLifecycleService(
            SchoolOnboardingWorkflowService onboardingService
    ) {

        this.onboardingService = onboardingService;
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
    }
}
