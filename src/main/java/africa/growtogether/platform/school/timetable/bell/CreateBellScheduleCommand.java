package africa.growtogether.platform.school.timetable.bell;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBellScheduleCommand(

        @NotNull
        UUID campusId,

        @NotBlank
        String scheduleCode,

        @NotBlank
        String scheduleName,

        String description,

        @NotBlank
        String scheduleType,

        @NotNull
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        Boolean mondayEnabled,
        Boolean tuesdayEnabled,
        Boolean wednesdayEnabled,
        Boolean thursdayEnabled,
        Boolean fridayEnabled,
        Boolean saturdayEnabled,
        Boolean sundayEnabled

) {
}
