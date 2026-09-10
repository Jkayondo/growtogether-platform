package africa.growtogether.platform.school.assessment.examination;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateExaminationSessionCommand(
        @NotBlank String sessionCode,
        @NotBlank String sessionName,
        String description,
        @NotNull UUID academicYearId,
        UUID academicTermId,
        @NotNull UUID campusId,
        @NotBlank String examinationType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        LocalDate registrationOpenDate,
        LocalDate registrationCloseDate,
        String externalAuthority,
        String externalSessionReference,
        UUID workflowInstanceId
) {
}
