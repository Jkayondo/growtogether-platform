package africa.growtogether.platform.school.profile;

import jakarta.validation.constraints.NotBlank;

public record CreateSchoolProfileCommand(

        @NotBlank
        String schoolCode,

        @NotBlank
        String schoolName,

        String legalName,

        String educationSystem,

        String countryCode,

        String defaultCurrency,

        String timezone,

        String email,

        String phoneNumber,

        String website

) {
}
