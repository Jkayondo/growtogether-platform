package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;
import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;
import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TeacherProgrammeLessonDetailService {

    private final TeacherProgrammeDayService days;

    private final TeacherProgrammeLessonService lessons;

    private final BellPeriodRepository bellPeriods;

    private final SubjectOfferingRepository subjectOfferings;

    private final SubjectRepository subjects;

    private final ClassGradeRepository classGrades;

    private final StreamRepository streams;

    public TeacherProgrammeLessonDetailService(
            TeacherProgrammeDayService days,
            TeacherProgrammeLessonService lessons,
            BellPeriodRepository bellPeriods,
            SubjectOfferingRepository subjectOfferings,
            SubjectRepository subjects,
            ClassGradeRepository classGrades,
            StreamRepository streams
    ) {

        this.days = days;
        this.lessons = lessons;
        this.bellPeriods = bellPeriods;
        this.subjectOfferings = subjectOfferings;
        this.subjects = subjects;
        this.classGrades = classGrades;
        this.streams = streams;
    }

    /**
     * Builds the display-ready lesson portion of Today Programme.
     *
     * All reference-data lookups remain tenant-scoped. The method
     * reuses the already verified recurrence-filtered lesson service
     * and resolves ProgrammeDay only once for the complete operation.
     */
    @Transactional(readOnly = true)
    public List<TeacherProgrammeLessonDetail> currentLessonDetails() {

        return currentLessonDetails(
                days.currentDay()
        );
    }

    /*
     * Package-private so the Today Programme aggregate can resolve
     * ProgrammeDay once and reuse exactly the same authenticated
     * tenant, teacher and school-local date context for lessons and
     * calendar events.
     */
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

        if (entries.isEmpty()) {
            return List.of();
        }

        Map<UUID, BellPeriod> bellPeriodCache =
                new HashMap<>();

        Map<UUID, SubjectOffering> subjectOfferingCache =
                new HashMap<>();

        Map<UUID, Subject> subjectCache =
                new HashMap<>();

        Map<UUID, ClassGrade> classGradeCache =
                new HashMap<>();

        Map<UUID, Stream> streamCache =
                new HashMap<>();

        return entries.stream()
                .map(
                        entry -> enrich(
                                day.tenantId(),
                                entry,
                                bellPeriodCache,
                                subjectOfferingCache,
                                subjectCache,
                                classGradeCache,
                                streamCache
                        )
                )
                .sorted(
                        Comparator
                                .comparing(
                                        TeacherProgrammeLessonDetail::sequenceNumber,
                                        Comparator.nullsLast(
                                                Integer::compareTo
                                        )
                                )
                                .thenComparing(
                                        TeacherProgrammeLessonDetail::startTime,
                                        Comparator.nullsLast(
                                                LocalTime::compareTo
                                        )
                                )
                                .thenComparing(
                                        TeacherProgrammeLessonDetail::periodCode,
                                        Comparator.nullsLast(
                                                String::compareTo
                                        )
                                )
                )
                .toList();
    }

    private TeacherProgrammeLessonDetail enrich(
            UUID tenantId,
            TimetableEntry entry,
            Map<UUID, BellPeriod> bellPeriodCache,
            Map<UUID, SubjectOffering> subjectOfferingCache,
            Map<UUID, Subject> subjectCache,
            Map<UUID, ClassGrade> classGradeCache,
            Map<UUID, Stream> streamCache
    ) {

        UUID bellPeriodId =
                requireId(
                        entry.getBellPeriodId(),
                        "Teacher programme lesson has no bell period"
                );

        UUID subjectOfferingId =
                requireId(
                        entry.getSubjectOfferingId(),
                        "Teacher programme lesson has no subject offering"
                );

        UUID classGradeId =
                requireId(
                        entry.getClassGradeId(),
                        "Teacher programme lesson has no class grade"
                );

        BellPeriod bellPeriod =
                bellPeriodCache.computeIfAbsent(
                        bellPeriodId,
                        id -> requireBellPeriod(
                                tenantId,
                                id
                        )
                );

        SubjectOffering subjectOffering =
                subjectOfferingCache.computeIfAbsent(
                        subjectOfferingId,
                        id -> requireSubjectOffering(
                                tenantId,
                                id
                        )
                );

        UUID subjectId =
                requireId(
                        subjectOffering.getSubjectId(),
                        "Teacher programme subject offering has no subject"
                );

        Subject subject =
                subjectCache.computeIfAbsent(
                        subjectId,
                        id -> requireSubject(
                                tenantId,
                                id
                        )
                );

        ClassGrade classGrade =
                classGradeCache.computeIfAbsent(
                        classGradeId,
                        id -> requireClassGrade(
                                tenantId,
                                id
                        )
                );

        UUID streamId =
                entry.getStreamId();

        Stream stream =
                streamId == null
                        ? null
                        : streamCache.computeIfAbsent(
                                streamId,
                                id -> requireStream(
                                        tenantId,
                                        id
                                )
                        );

        return new TeacherProgrammeLessonDetail(
                entry.getTimetableId(),
                bellPeriodId,
                bellPeriod.getPeriodCode(),
                bellPeriod.getPeriodName(),
                bellPeriod.getSequenceNumber(),
                bellPeriod.getStartTime(),
                bellPeriod.getEndTime(),
                classGradeId,
                classGrade.getClassCode(),
                classGrade.getClassName(),
                streamId,
                stream == null
                        ? null
                        : stream.getStreamCode(),
                stream == null
                        ? null
                        : stream.getStreamName(),
                subjectOfferingId,
                subjectId,
                subject.getSubjectCode(),
                subject.getSubjectName(),
                entry.getActivityName()
        );
    }

    private BellPeriod requireBellPeriod(
            UUID tenantId,
            UUID id
    ) {

        return bellPeriods
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Teacher programme bell period not found for tenant"
                        )
                );
    }

    private SubjectOffering requireSubjectOffering(
            UUID tenantId,
            UUID id
    ) {

        return subjectOfferings
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Teacher programme subject offering not found for tenant"
                        )
                );
    }

    private Subject requireSubject(
            UUID tenantId,
            UUID id
    ) {

        return subjects
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Teacher programme subject not found for tenant"
                        )
                );
    }

    private ClassGrade requireClassGrade(
            UUID tenantId,
            UUID id
    ) {

        return classGrades
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Teacher programme class grade not found for tenant"
                        )
                );
    }

    private Stream requireStream(
            UUID tenantId,
            UUID id
    ) {

        return streams
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Teacher programme stream not found for tenant"
                        )
                );
    }

    private UUID requireId(
            UUID id,
            String message
    ) {

        if (id == null) {
            throw new IllegalStateException(
                    message
            );
        }

        return id;
    }
}
