package africa.growtogether.platform.school.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAdmissionApplicantCommand(

        @NotBlank
        String firstName,

        String middleName,

        @NotBlank
        String lastName,

        String preferredName,

        @NotNull
        LocalDate dateOfBirth,

        String gender,

        String nationalityCode,

        String countryOfBirthCode,

        String primaryLanguage,

        String religion,

        String nationalIdNumber,

        String passportNumber,

        String birthCertificateNumber,

        String email,

        String phoneNumber,

        String physicalAddress,

        UUID existingEiamUserId,

        UUID existingLearnerReference

) {
}
