package africa.growtogether.platform.school.learner.programme;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import africa.growtogether.platform.school.timetable.occurrence.TimetableOccurrenceResolver;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearnerProgrammeLessonServiceTest {

    @Test
    void resolvesClassAndStreamProgrammeUsingSharedOccurrenceRules() {

        LearnerProgrammeDayService days =
                mock(LearnerProgrammeDayService.class);

        LearnerProgrammeLessonRepository lessons =
                mock(LearnerProgrammeLessonRepository.class);

        TimetableRepository timetables =
                mock(TimetableRepository.class);

        TimetableOccurrenceResolver occurrences =
                mock(TimetableOccurrenceResolver.class);

        UUID tenantId = UUID.randomUUID();
        UUID learnerId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID academicYearId = UUID.randomUUID();
        UUID academicTermId = UUID.randomUUID();
        UUID campusId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();

        LocalDate date =
                LocalDate.of(
                        2026,
                        9,
                        24
                );

        ZoneId zone =
                ZoneId.of(
                        "Africa/Kampala"
                );

        LearnerProgrammeDayService.ProgrammeDay day =
                new LearnerProgrammeDayService.ProgrammeDay(
                        tenantId,
                        learnerId,
                        enrollmentId,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        date,
                        zone,
                        date.atStartOfDay(zone).toInstant(),
                        date.plusDays(1)
                                .atStartOfDay(zone)
                                .toInstant()
                );

        TimetableEntry entry =
                mock(TimetableEntry.class);

        Timetable timetable =
                mock(Timetable.class);

        when(days.currentDay())
                .thenReturn(day);

        when(
                lessons.findLessonCandidates(
                        tenantId,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        date,
                        date.getDayOfWeek().name(),
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(entry)
        );

        when(entry.getTimetableId())
                .thenReturn(timetableId);

        when(entry.getDayOfWeek())
                .thenReturn(
                        date.getDayOfWeek()
                                .name()
                );

        when(
                timetables.findByTenantIdAndId(
                        tenantId,
                        timetableId
                )
        ).thenReturn(
                Optional.of(timetable)
        );

        when(
                occurrences.occursOn(
                        entry.getDayOfWeek(),
                        entry.isRecurring(),
                        entry.getRecurrenceRule(),
                        entry.getEffectiveFrom(),
                        entry.getEffectiveTo(),
                        timetable.getEffectiveFrom(),
                        timetable.getEffectiveTo(),
                        date
                )
        ).thenReturn(true);

        LearnerProgrammeLessonService service =
                new LearnerProgrammeLessonService(
                        days,
                        lessons,
                        timetables,
                        occurrences
                );

        assertThat(
                service.currentLessons()
        ).containsExactly(entry);

        verify(lessons)
                .findLessonCandidates(
                        tenantId,
                        academicYearId,
                        academicTermId,
                        campusId,
                        classGradeId,
                        streamId,
                        date,
                        date.getDayOfWeek().name(),
                        EntityStatus.ACTIVE
                );
    }
}
