package africa.growtogether.platform.school.timetable.availability;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateTeacherUnavailabilityCommand(

        @NotNull
        UUID teacherProfileId,

        @NotNull
        Instant unavailableFrom,

        @NotNull
        Instant unavailableTo,

        @NotBlank
        String reasonType,

        String reason,

        Boolean recurring,

        String recurrenceRule,

        UUID leaveExtensionId

) {
}
