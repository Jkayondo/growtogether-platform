package africa.growtogether.platform.school.programme;

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

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared display projection for timetable lesson entries.
 *
 * Identity, authorization and candidate selection stay with the calling
 * Teacher or Learner service. This component only enriches already
 * authorized timetable entries using tenant-scoped reference data.
 */
@Service
public class ProgrammeLessonDetailProjector {

    private final BellPeriodRepository bellPeriods;
    private final SubjectOfferingRepository subjectOfferings;
    private final SubjectRepository subjects;
    private final ClassGradeRepository classGrades;
    private final StreamRepository streams;

    public ProgrammeLessonDetailProjector(
            BellPeriodRepository bellPeriods,
            SubjectOfferingRepository subjectOfferings,
            SubjectRepository subjects,
            ClassGradeRepository classGrades,
            StreamRepository streams
    ) {
        this.bellPeriods = bellPeriods;
        this.subjectOfferings = subjectOfferings;
        this.subjects = subjects;
        this.classGrades = classGrades;
        this.streams = streams;
    }

    public List<ProgrammeLessonDetail> project(
            UUID tenantId,
            List<TimetableEntry> entries
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (entries == null) {
            throw new IllegalArgumentException(
                    "entries must not be null"
            );
        }

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

        return entries
                .stream()
                .map(
                        entry -> enrich(
                                tenantId,
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
                                        ProgrammeLessonDetail::sequenceNumber,
                                        Comparator.nullsLast(
                                                Integer::compareTo
                                        )
                                )
                                .thenComparing(
                                        ProgrammeLessonDetail::startTime,
                                        Comparator.nullsLast(
                                                LocalTime::compareTo
                                        )
                                )
                                .thenComparing(
                                        ProgrammeLessonDetail::periodCode,
                                        Comparator.nullsLast(
                                                String::compareTo
                                        )
                                )
                )
                .toList();
    }

    private ProgrammeLessonDetail enrich(
            UUID tenantId,
            TimetableEntry entry,
            Map<UUID, BellPeriod> bellPeriodCache,
            Map<UUID, SubjectOffering> subjectOfferingCache,
            Map<UUID, Subject> subjectCache,
            Map<UUID, ClassGrade> classGradeCache,
            Map<UUID, Stream> streamCache
    ) {
        if (entry == null) {
            throw new IllegalStateException(
                    "Programme lesson projection received null entry"
            );
        }

        UUID bellPeriodId =
                requireId(
                        entry.getBellPeriodId(),
                        "Programme lesson has no bell period"
                );

        UUID subjectOfferingId =
                requireId(
                        entry.getSubjectOfferingId(),
                        "Programme lesson has no subject offering"
                );

        UUID classGradeId =
                requireId(
                        entry.getClassGradeId(),
                        "Programme lesson has no class grade"
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
                        "Programme subject offering has no subject"
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

        return new ProgrammeLessonDetail(
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
                                "Programme bell period not found for tenant"
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
                                "Programme subject offering not found for tenant"
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
                                "Programme subject not found for tenant"
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
                                "Programme class grade not found for tenant"
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
                                "Programme stream not found for tenant"
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
