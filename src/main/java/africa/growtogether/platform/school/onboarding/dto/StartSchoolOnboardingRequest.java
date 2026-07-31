package africa.growtogether.platform.school.onboarding.dto;


import java.util.UUID;


public record StartSchoolOnboardingRequest(

        UUID schoolConfigurationId

) {
}
