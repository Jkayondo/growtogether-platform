package africa.growtogether.platform.school.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateStudentCommand(

        UUID admissionApplicationId,

        @NotBlank
        String studentNumber,

        @NotBlank
        String permanentLearnerNumber,

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

        String email,

        String phoneNumber,

        String physicalAddress,

        UUID eiamUserId,

        UUID edsStudentFileId,

        LocalDate admissionDate,

        LocalDate firstEnrollmentDate,

        LocalDate expectedCompletionDate,

        LocalDate completionDate

) {
}
