package africa.growtogether.platform.school.timetable.generation;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTimetableGenerationRequestCommand(
        String generationCode,
        UUID academicYearId,
        UUID academicTermId,
        UUID campusId,
        UUID bellScheduleId,
        String timetableType,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String generationMode,
        String modelCode,
        String objectives,
        UUID requestedBy
) {
}
