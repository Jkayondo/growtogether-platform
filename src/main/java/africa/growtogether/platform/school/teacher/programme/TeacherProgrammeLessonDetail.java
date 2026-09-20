package africa.growtogether.platform.school.teacher.programme;

import java.time.LocalTime;
import java.util.UUID;

public record TeacherProgrammeLessonDetail(
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
