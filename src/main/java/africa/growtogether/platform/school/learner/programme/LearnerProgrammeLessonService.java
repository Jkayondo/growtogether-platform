package africa.growtogether.platform.school.learner.programme;

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
public class LearnerProgrammeLessonService {

    private final LearnerProgrammeDayService days;
    private final LearnerProgrammeLessonRepository lessons;
    private final TimetableRepository timetables;
    private final TimetableOccurrenceResolver occurrences;

    public LearnerProgrammeLessonService(
            LearnerProgrammeDayService days,
            LearnerProgrammeLessonRepository lessons,
            TimetableRepository timetables,
            TimetableOccurrenceResolver occurrences
    ) {
        this.days = days;
        this.lessons = lessons;
        this.timetables = timetables;
        this.occurrences = occurrences;
    }

    @Transactional(readOnly = true)
    public List<TimetableEntry> currentLessons() {

        return currentLessons(
                days.currentDay()
        );
    }

    List<TimetableEntry> currentLessons(
            LearnerProgrammeDayService.ProgrammeDay day
    ) {

        if (day == null) {
            throw new IllegalArgumentException(
                    "programme day must not be null"
            );
        }

        List<TimetableEntry> candidates =
                lessons.findLessonCandidates(
                        day.tenantId(),
                        day.academicYearId(),
                        day.academicTermId(),
                        day.campusId(),
                        day.classGradeId(),
                        day.streamId(),
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

        return candidates
                .stream()
                .filter(
                        entry ->
                                occursToday(
                                        day,
                                        entry,
                                        timetableCache
                                )
                )
                .toList();
    }

    private boolean occursToday(
            LearnerProgrammeDayService.ProgrammeDay day,
            TimetableEntry entry,
            Map<UUID, Timetable> timetableCache
    ) {

        if (entry == null) {
            throw new IllegalStateException(
                    "Learner programme query returned null timetable entry."
            );
        }

        UUID timetableId =
                entry.getTimetableId();

        if (timetableId == null) {
            throw new IllegalStateException(
                    "Learner programme candidate has no timetable."
            );
        }

        Timetable timetable =
                timetableCache.computeIfAbsent(
                        timetableId,
                        id ->
                                timetables
                                        .findByTenantIdAndId(
                                                day.tenantId(),
                                                id
                                        )
                                        .orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                "Learner programme timetable not found for tenant."
                                                        )
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
}
