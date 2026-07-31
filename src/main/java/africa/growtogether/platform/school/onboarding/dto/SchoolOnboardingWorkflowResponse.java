package africa.growtogether.platform.school.onboarding.dto;


import africa.growtogether.platform.school.onboarding.SchoolOnboardingStatus;

import java.time.Instant;
import java.util.UUID;


public record SchoolOnboardingWorkflowResponse(

        UUID id,

        UUID schoolConfigurationId,

        SchoolOnboardingStatus status,

        Instant startedAt,

        Instant completedAt

) {
}
