package africa.growtogether.platform.school.guardian;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record CreateGuardianCommand(

        @NotBlank
        String guardianNumber,

        @NotBlank
        String firstName,

        String middleName,

        @NotBlank
        String lastName,

        String preferredName,

        LocalDate dateOfBirth,

        String gender,

        String nationalityCode,

        String nationalIdNumber,

        String passportNumber,

        @NotBlank
        String primaryPhoneNumber,

        String alternativePhoneNumber,

        @Email
        String email,

        String physicalAddress,

        String postalAddress,

        String occupation,

        String employer,

        UUID eiamUserId,

        UUID sourceAdmissionGuardianId,

        String preferredLanguage

) {
}
