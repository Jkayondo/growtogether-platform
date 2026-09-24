package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;
import africa.growtogether.platform.school.programme.ProgrammeLessonDetail;
import africa.growtogether.platform.school.programme.ProgrammeLessonDetailProjector;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Teacher-specific adapter over the shared school programme lesson
 * projection. Teacher identity and lesson candidate authorization remain
 * the responsibility of TeacherProgrammeDayService and
 * TeacherProgrammeLessonService.
 */
@Service
public class TeacherProgrammeLessonDetailService {

    private final TeacherProgrammeDayService days;
    private final TeacherProgrammeLessonService lessons;
    private final ProgrammeLessonDetailProjector projector;

    @Autowired
    public TeacherProgrammeLessonDetailService(
            TeacherProgrammeDayService days,
            TeacherProgrammeLessonService lessons,
            ProgrammeLessonDetailProjector projector
    ) {
        this.days = days;
        this.lessons = lessons;
        this.projector = projector;
    }

    /**
     * Compatibility constructor retained for existing focused tests and
     * callers while projection ownership moves to the shared capability.
     */
    public TeacherProgrammeLessonDetailService(
            TeacherProgrammeDayService days,
            TeacherProgrammeLessonService lessons,
            BellPeriodRepository bellPeriods,
            SubjectOfferingRepository subjectOfferings,
            SubjectRepository subjects,
            ClassGradeRepository classGrades,
            StreamRepository streams
    ) {
        this(
                days,
                lessons,
                new ProgrammeLessonDetailProjector(
                        bellPeriods,
                        subjectOfferings,
                        subjects,
                        classGrades,
                        streams
                )
        );
    }

    @Transactional(readOnly = true)
    public List<TeacherProgrammeLessonDetail> currentLessonDetails() {
        return currentLessonDetails(
                days.currentDay()
        );
    }

    List<TeacherProgrammeLessonDetail> currentLessonDetails(
            TeacherProgrammeDayService.ProgrammeDay day
    ) {
        if (day == null) {
            throw new IllegalArgumentException(
                    "programme day must not be null"
            );
        }

        List<TimetableEntry> entries =
                lessons.currentLessons(
                        day
                );

        try {
            return projector
                    .project(
                            day.tenantId(),
                            entries
                    )
                    .stream()
                    .map(
                            TeacherProgrammeLessonDetailService::toTeacherDetail
                    )
                    .toList();
        } catch (IllegalStateException ex) {
            throw preserveTeacherProgrammeErrorContract(
                    ex
            );
        }
    }

    /**
     * Preserve the established Teacher programme failure contract while
     * projection internals are shared across Teacher and Learner.
     */
    private static IllegalStateException
    preserveTeacherProgrammeErrorContract(
            IllegalStateException ex
    ) {
        String message =
                ex.getMessage();

        if (message == null) {
            return ex;
        }

        String teacherMessage =
                switch (message) {
                    case "Programme bell period not found for tenant" ->
                            "Teacher programme bell period not found for tenant";

                    case "Programme subject offering not found for tenant" ->
                            "Teacher programme subject offering not found for tenant";

                    case "Programme subject not found for tenant" ->
                            "Teacher programme subject not found for tenant";

                    case "Programme class grade not found for tenant" ->
                            "Teacher programme class grade not found for tenant";

                    case "Programme stream not found for tenant" ->
                            "Teacher programme stream not found for tenant";

                    case "Programme lesson has no bell period" ->
                            "Teacher programme lesson has no bell period";

                    case "Programme lesson has no subject offering" ->
                            "Teacher programme lesson has no subject offering";

                    case "Programme lesson has no class grade" ->
                            "Teacher programme lesson has no class grade";

                    default ->
                            null;
                };

        if (teacherMessage == null) {
            return ex;
        }

        return new IllegalStateException(
                teacherMessage,
                ex
        );
    }

    private static TeacherProgrammeLessonDetail toTeacherDetail(
            ProgrammeLessonDetail detail
    ) {
        return new TeacherProgrammeLessonDetail(
                detail.timetableId(),
                detail.bellPeriodId(),
                detail.periodCode(),
                detail.periodName(),
                detail.sequenceNumber(),
                detail.startTime(),
                detail.endTime(),
                detail.classGradeId(),
                detail.classCode(),
                detail.className(),
                detail.streamId(),
                detail.streamCode(),
                detail.streamName(),
                detail.subjectOfferingId(),
                detail.subjectId(),
                detail.subjectCode(),
                detail.subjectName(),
                detail.activityName()
        );
    }
}
