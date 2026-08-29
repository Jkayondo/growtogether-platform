package africa.growtogether.platform.school.timetable.entry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTimetableEntryCommand(

        @NotNull
        UUID timetableId,

        @NotNull
        UUID bellPeriodId,

        @NotBlank
        String dayOfWeek,

        UUID classOfferingId,

        UUID subjectOfferingId,

        UUID classGradeId,

        UUID streamId,

        UUID teachingAssignmentId,

        UUID teacherProfileId,

        UUID schedulingResourceId,

        @NotBlank
        String entryType,

        String activityName,

        String notes,

        Boolean recurring,

        String recurrenceRule,

        LocalDate effectiveFrom,

        LocalDate effectiveTo

) {
}
