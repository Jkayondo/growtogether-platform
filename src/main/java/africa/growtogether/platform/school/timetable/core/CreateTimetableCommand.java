package africa.growtogether.platform.school.timetable.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTimetableCommand(

        @NotBlank
        String timetableCode,

        @NotBlank
        String timetableName,

        String description,

        @NotNull
        UUID academicYearId,

        UUID academicTermId,

        @NotNull
        UUID campusId,

        @NotNull
        UUID bellScheduleId,

        @NotBlank
        String timetableType,

        @Positive
        Integer versionNumber,

        @NotNull
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        String generatedBy,

        UUID generationReference,

        UUID workflowInstanceId

) {
}
