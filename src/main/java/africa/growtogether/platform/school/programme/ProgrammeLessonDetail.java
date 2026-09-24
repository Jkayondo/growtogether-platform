package africa.growtogether.platform.school.programme;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Display-ready school programme lesson projection shared by
 * authenticated Teacher and Learner experiences.
 */
public record ProgrammeLessonDetail(
        UUID timetableId,
        UUID bellPeriodId,
        String periodCode,
        String periodName,
        Integer sequenceNumber,
        LocalTime startTime,
        LocalTime endTime,
        UUID classGradeId,
        String classCode,
        String className,
        UUID streamId,
        String streamCode,
        String streamName,
        UUID subjectOfferingId,
        UUID subjectId,
        String subjectCode,
        String subjectName,
        String activityName
) {
}
