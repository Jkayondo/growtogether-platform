package africa.growtogether.platform.school.timetable.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateSchedulingResourceCommand(

        @NotNull
        UUID campusId,

        @NotBlank
        String resourceCode,

        @NotBlank
        String resourceName,

        @NotBlank
        String resourceType,

        @Positive
        Integer capacity,

        String locationDescription,

        UUID specializedForSubjectId,

        boolean bookable,

        boolean sharedResource

) {
}
