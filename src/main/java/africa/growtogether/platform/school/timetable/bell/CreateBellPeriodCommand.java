package africa.growtogether.platform.school.timetable.bell;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.UUID;

public record CreateBellPeriodCommand(

        @NotNull
        UUID bellScheduleId,

        @NotBlank
        String periodCode,

        @NotBlank
        String periodName,

        @Positive
        Integer sequenceNumber,

        @NotBlank
        String periodType,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        Integer instructionalMinutes,

        Boolean attendanceRequired,

        Boolean schedulingAllowed

) {
}
