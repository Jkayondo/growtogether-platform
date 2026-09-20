package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import africa.growtogether.platform.school.timetable.occurrence.TimetableOccurrenceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TeacherProgrammeLessonService {

    private final TeacherProgrammeDayService days;
    private final TeacherProgrammeLessonRepository lessons;
    private final TimetableRepository timetables;
    private final TimetableOccurrenceResolver occurrences;

    public TeacherProgrammeLessonService(
            TeacherProgrammeDayService days,
            TeacherProgrammeLessonRepository lessons,
            TimetableRepository timetables,
            TimetableOccurrenceResolver occurrences
    ) {

        this.days = days;
        this.lessons = lessons;
        this.timetables = timetables;
        this.occurrences = occurrences;
    }

    /**
     * Returns lesson entries that genuinely occur on the authenticated
     * teacher's current school-local date.
     *
     * Candidate selection remains database-backed and tenant-scoped.
     * Recurrence semantics are delegated to the shared timetable
     * occurrence capability rather than duplicated here.
     */
    @Transactional(readOnly = true)
    public List<TimetableEntry> currentLessons() {

        return currentLessons(
                days.currentDay()
        );
    }

    /*
     * Package-private deliberately:
     *
     * Today Programme enrichment resolves ProgrammeDay once and passes
     * the exact same tenant/teacher/date context into recurrence
     * filtering, avoiding a second clock/identity resolution.
     */
    List<TimetableEntry> currentLessons(
            TeacherProgrammeDayService.ProgrammeDay day
    ) {

        if (day == null) {
            throw new IllegalArgumentException(
                    "programme day must not be null"
            );
        }

        List<TimetableEntry> candidates =
                lessons.findLessonCandidates(
                        day.tenantId(),
                        day.teacherProfileId(),
                        day.date(),
                        day.date()
                                .getDayOfWeek()
                                .name(),
                        EntityStatus.ACTIVE
                );

        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<UUID, Timetable> timetableCache =
                new LinkedHashMap<>();

        return candidates.stream()
                .filter(
                        entry -> occursToday(
                                day,
                                entry,
                                timetableCache
                        )
                )
                .toList();
    }

    private boolean occursToday(
            TeacherProgrammeDayService.ProgrammeDay day,
            TimetableEntry entry,
            Map<UUID, Timetable> timetableCache
    ) {

        UUID timetableId =
                entry.getTimetableId();

        if (timetableId == null) {
            throw new IllegalStateException(
                    "Teacher programme candidate has no timetable"
            );
        }

        Timetable timetable =
                timetableCache.computeIfAbsent(
                        timetableId,
                        id -> requireTimetable(
                                day.tenantId(),
                                id
                        )
                );

        return occurrences.occursOn(
                entry.getDayOfWeek(),
                entry.isRecurring(),
                entry.getRecurrenceRule(),
                entry.getEffectiveFrom(),
                entry.getEffectiveTo(),
                timetable.getEffectiveFrom(),
                timetable.getEffectiveTo(),
                day.date()
        );
    }

    private Timetable requireTimetable(
            UUID tenantId,
            UUID timetableId
    ) {

        return timetables
                .findByTenantIdAndId(
                        tenantId,
                        timetableId
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Teacher programme timetable not found for tenant"
                        )
                );
    }
}
