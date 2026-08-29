package africa.growtogether.platform.school.admission;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAdmissionApplicationCommand(

        @NotNull
        UUID academicYearId,

        @NotNull
        UUID campusId,

        @NotNull
        UUID desiredClassGradeId,

        UUID desiredStreamId,

        String submissionChannel

) {
}
