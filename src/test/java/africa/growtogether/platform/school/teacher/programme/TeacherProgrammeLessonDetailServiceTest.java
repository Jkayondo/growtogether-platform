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
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeacherProgrammeLessonDetailServiceTest {

    @Test
    void enrichesLessonUsingTenantScopedReferenceData() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.stubDay();

        UUID timetableId = UUID.randomUUID();
        UUID bellPeriodId = UUID.randomUUID();
        UUID subjectOfferingId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        timetableId,
                        bellPeriodId,
                        subjectOfferingId,
                        classGradeId,
                        streamId,
                        "Fractions"
                );

        BellPeriod bell =
                f.bell(
                        "P2",
                        "Period 2",
                        2,
                        LocalTime.of(9, 20),
                        LocalTime.of(10, 0)
                );

        SubjectOffering offering =
                f.subjectOffering(
                        subjectId
                );

        Subject subject =
                f.subject(
                        "MATH",
                        "Mathematics"
                );

        ClassGrade classGrade =
                f.classGrade(
                        "P6",
                        "Primary Six"
                );

        Stream stream =
                f.stream(
                        "A",
                        "Stream A"
                );

        when(
                f.lessons.currentLessons(
                        same(day)
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.bellPeriods.findByTenantIdAndId(
                        f.tenantId,
                        bellPeriodId
                )
        ).thenReturn(
                Optional.of(bell)
        );

        when(
                f.subjectOfferings.findByTenantIdAndId(
                        f.tenantId,
                        subjectOfferingId
                )
        ).thenReturn(
                Optional.of(offering)
        );

        when(
                f.subjects.findByTenantIdAndId(
                        f.tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(subject)
        );

        when(
                f.classGrades.findByTenantIdAndId(
                        f.tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                f.streams.findByTenantIdAndId(
                        f.tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        List<TeacherProgrammeLessonDetail> result =
                f.service.currentLessonDetails();

        assertEquals(
                1,
                result.size()
        );

        TeacherProgrammeLessonDetail detail =
                result.get(0);

        assertEquals(
                timetableId,
                detail.timetableId()
        );

        assertEquals(
                bellPeriodId,
                detail.bellPeriodId()
        );

        assertEquals(
                "P2",
                detail.periodCode()
        );

        assertEquals(
                "Period 2",
                detail.periodName()
        );

        assertEquals(
                2,
                detail.sequenceNumber()
        );

        assertEquals(
                LocalTime.of(9, 20),
                detail.startTime()
        );

        assertEquals(
                LocalTime.of(10, 0),
                detail.endTime()
        );

        assertEquals(
                classGradeId,
                detail.classGradeId()
        );

        assertEquals(
                "P6",
                detail.classCode()
        );

        assertEquals(
                "Primary Six",
                detail.className()
        );

        assertEquals(
                streamId,
                detail.streamId()
        );

        assertEquals(
                "A",
                detail.streamCode()
        );

        assertEquals(
                "Stream A",
                detail.streamName()
        );

        assertEquals(
                subjectOfferingId,
                detail.subjectOfferingId()
        );

        assertEquals(
                subjectId,
                detail.subjectId()
        );

        assertEquals(
                "MATH",
                detail.subjectCode()
        );

        assertEquals(
                "Mathematics",
                detail.subjectName()
        );

        assertEquals(
                "Fractions",
                detail.activityName()
        );

        verify(
                f.days,
                times(1)
        ).currentDay();

        verify(
                f.lessons,
                times(1)
        ).currentLessons(
                same(day)
        );

        verify(
                f.bellPeriods
        ).findByTenantIdAndId(
                f.tenantId,
                bellPeriodId
        );

        verify(
                f.subjectOfferings
        ).findByTenantIdAndId(
                f.tenantId,
                subjectOfferingId
        );

        verify(
                f.subjects
        ).findByTenantIdAndId(
                f.tenantId,
                subjectId
        );

        verify(
                f.classGrades
        ).findByTenantIdAndId(
                f.tenantId,
                classGradeId
        );

        verify(
                f.streams
        ).findByTenantIdAndId(
                f.tenantId,
                streamId
        );
    }

    @Test
    void allowsLessonWithoutStreamAndDoesNotPerformStreamLookup() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.stubDay();

        UUID bellPeriodId = UUID.randomUUID();
        UUID subjectOfferingId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        UUID.randomUUID(),
                        bellPeriodId,
                        subjectOfferingId,
                        classGradeId,
                        null,
                        null
                );

        BellPeriod bell =
                f.bell(
                        "P1",
                        "Period 1",
                        1,
                        LocalTime.of(8, 30),
                        LocalTime.of(9, 10)
                );

        SubjectOffering offering =
                f.subjectOffering(
                        subjectId
                );

        Subject subject =
                f.subject(
                        "ENG",
                        "English"
                );

        ClassGrade classGrade =
                f.classGrade(
                        "P5",
                        "Primary Five"
                );

        when(
                f.lessons.currentLessons(
                        same(day)
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.bellPeriods.findByTenantIdAndId(
                        f.tenantId,
                        bellPeriodId
                )
        ).thenReturn(
                Optional.of(bell)
        );

        when(
                f.subjectOfferings.findByTenantIdAndId(
                        f.tenantId,
                        subjectOfferingId
                )
        ).thenReturn(
                Optional.of(offering)
        );

        when(
                f.subjects.findByTenantIdAndId(
                        f.tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(subject)
        );

        when(
                f.classGrades.findByTenantIdAndId(
                        f.tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        TeacherProgrammeLessonDetail detail =
                f.service
                        .currentLessonDetails()
                        .get(0);

        assertNull(
                detail.streamId()
        );

        assertNull(
                detail.streamCode()
        );

        assertNull(
                detail.streamName()
        );

        verify(
                f.streams,
                never()
        ).findByTenantIdAndId(
                any(),
                any()
        );
    }

    @Test
    void ordersLessonsByBellSequenceAndReusesReferenceCaches() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.stubDay();

        UUID firstBellId = UUID.randomUUID();
        UUID secondBellId = UUID.randomUUID();

        UUID subjectOfferingId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        TimetableEntry later =
                f.entry(
                        UUID.randomUUID(),
                        secondBellId,
                        subjectOfferingId,
                        classGradeId,
                        streamId,
                        "Later lesson"
                );

        TimetableEntry earlier =
                f.entry(
                        UUID.randomUUID(),
                        firstBellId,
                        subjectOfferingId,
                        classGradeId,
                        streamId,
                        "Earlier lesson"
                );

        BellPeriod firstBell =
                f.bell(
                        "P1",
                        "Period 1",
                        1,
                        LocalTime.of(8, 0),
                        LocalTime.of(8, 40)
                );

        BellPeriod secondBell =
                f.bell(
                        "P2",
                        "Period 2",
                        2,
                        LocalTime.of(8, 40),
                        LocalTime.of(9, 20)
                );

        SubjectOffering offering =
                f.subjectOffering(
                        subjectId
                );

        Subject subject =
                f.subject(
                        "SCI",
                        "Science"
                );

        ClassGrade classGrade =
                f.classGrade(
                        "P7",
                        "Primary Seven"
                );

        Stream stream =
                f.stream(
                        "BLUE",
                        "Blue"
                );

        when(
                f.lessons.currentLessons(
                        same(day)
                )
        ).thenReturn(
                List.of(
                        later,
                        earlier
                )
        );

        when(
                f.bellPeriods.findByTenantIdAndId(
                        f.tenantId,
                        firstBellId
                )
        ).thenReturn(
                Optional.of(firstBell)
        );

        when(
                f.bellPeriods.findByTenantIdAndId(
                        f.tenantId,
                        secondBellId
                )
        ).thenReturn(
                Optional.of(secondBell)
        );

        when(
                f.subjectOfferings.findByTenantIdAndId(
                        f.tenantId,
                        subjectOfferingId
                )
        ).thenReturn(
                Optional.of(offering)
        );

        when(
                f.subjects.findByTenantIdAndId(
                        f.tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(subject)
        );

        when(
                f.classGrades.findByTenantIdAndId(
                        f.tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                f.streams.findByTenantIdAndId(
                        f.tenantId,
                        streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );

        List<TeacherProgrammeLessonDetail> result =
                f.service.currentLessonDetails();

        assertEquals(
                2,
                result.size()
        );

        assertSame(
                "Earlier lesson",
                result.get(0).activityName()
        );

        assertSame(
                "Later lesson",
                result.get(1).activityName()
        );

        verify(
                f.subjectOfferings,
                times(1)
        ).findByTenantIdAndId(
                f.tenantId,
                subjectOfferingId
        );

        verify(
                f.subjects,
                times(1)
        ).findByTenantIdAndId(
                f.tenantId,
                subjectId
        );

        verify(
                f.classGrades,
                times(1)
        ).findByTenantIdAndId(
                f.tenantId,
                classGradeId
        );

        verify(
                f.streams,
                times(1)
        ).findByTenantIdAndId(
                f.tenantId,
                streamId
        );
    }

    @Test
    void failsClosedWhenBellPeriodIsNotAvailableForTenant() {

        Fixture f = new Fixture();

        TeacherProgrammeDayService.ProgrammeDay day =
                f.stubDay();

        UUID bellPeriodId =
                UUID.randomUUID();

        TimetableEntry entry =
                f.entry(
                        UUID.randomUUID(),
                        bellPeriodId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        null
                );

        when(
                f.lessons.currentLessons(
                        same(day)
                )
        ).thenReturn(
                List.of(entry)
        );

        when(
                f.bellPeriods.findByTenantIdAndId(
                        f.tenantId,
                        bellPeriodId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> f.service.currentLessonDetails()
                );

        assertEquals(
                "Teacher programme bell period not found for tenant",
                error.getMessage()
        );

        verify(
                f.bellPeriods
        ).findByTenantIdAndId(
                f.tenantId,
                bellPeriodId
        );

        verify(
                f.subjectOfferings,
                never()
        ).findByTenantIdAndId(
                any(),
                any()
        );
    }

    private static class Fixture {

        final UUID tenantId =
                UUID.randomUUID();

        final UUID teacherId =
                UUID.randomUUID();

        final TeacherProgrammeDayService days =
                mock(
                        TeacherProgrammeDayService.class
                );

        final TeacherProgrammeLessonService lessons =
                mock(
                        TeacherProgrammeLessonService.class
                );

        final BellPeriodRepository bellPeriods =
                mock(
                        BellPeriodRepository.class
                );

        final SubjectOfferingRepository subjectOfferings =
                mock(
                        SubjectOfferingRepository.class
                );

        final SubjectRepository subjects =
                mock(
                        SubjectRepository.class
                );

        final ClassGradeRepository classGrades =
                mock(
                        ClassGradeRepository.class
                );

        final StreamRepository streams =
                mock(
                        StreamRepository.class
                );

        final TeacherProgrammeLessonDetailService service =
                new TeacherProgrammeLessonDetailService(
                        days,
                        lessons,
                        bellPeriods,
                        subjectOfferings,
                        subjects,
                        classGrades,
                        streams
                );

        TeacherProgrammeDayService.ProgrammeDay stubDay() {

            LocalDate date =
                    LocalDate.of(
                            2026,
                            9,
                            18
                    );

            ZoneId zone =
                    ZoneId.of(
                            "Africa/Kampala"
                    );

            TeacherProgrammeDayService.ProgrammeDay day =
                    new TeacherProgrammeDayService.ProgrammeDay(
                            tenantId,
                            teacherId,
                            date,
                            zone,
                            date.atStartOfDay(zone)
                                    .toInstant(),
                            date.plusDays(1)
                                    .atStartOfDay(zone)
                                    .toInstant()
                    );

            when(
                    days.currentDay()
            ).thenReturn(
                    day
            );

            return day;
        }

        TimetableEntry entry(
                UUID timetableId,
                UUID bellPeriodId,
                UUID subjectOfferingId,
                UUID classGradeId,
                UUID streamId,
                String activityName
        ) {

            TimetableEntry entry =
                    mock(
                            TimetableEntry.class
                    );

            when(
                    entry.getTimetableId()
            ).thenReturn(
                    timetableId
            );

            when(
                    entry.getBellPeriodId()
            ).thenReturn(
                    bellPeriodId
            );

            when(
                    entry.getSubjectOfferingId()
            ).thenReturn(
                    subjectOfferingId
            );

            when(
                    entry.getClassGradeId()
            ).thenReturn(
                    classGradeId
            );

            when(
                    entry.getStreamId()
            ).thenReturn(
                    streamId
            );

            when(
                    entry.getActivityName()
            ).thenReturn(
                    activityName
            );

            return entry;
        }

        BellPeriod bell(
                String code,
                String name,
                int sequence,
                LocalTime start,
                LocalTime end
        ) {

            BellPeriod bell =
                    mock(
                            BellPeriod.class
                    );

            when(
                    bell.getPeriodCode()
            ).thenReturn(
                    code
            );

            when(
                    bell.getPeriodName()
            ).thenReturn(
                    name
            );

            when(
                    bell.getSequenceNumber()
            ).thenReturn(
                    sequence
            );

            when(
                    bell.getStartTime()
            ).thenReturn(
                    start
            );

            when(
                    bell.getEndTime()
            ).thenReturn(
                    end
            );

            return bell;
        }

        SubjectOffering subjectOffering(
                UUID subjectId
        ) {

            SubjectOffering offering =
                    mock(
                            SubjectOffering.class
                    );

            when(
                    offering.getSubjectId()
            ).thenReturn(
                    subjectId
            );

            return offering;
        }

        Subject subject(
                String code,
                String name
        ) {

            Subject subject =
                    mock(
                            Subject.class
                    );

            when(
                    subject.getSubjectCode()
            ).thenReturn(
                    code
            );

            when(
                    subject.getSubjectName()
            ).thenReturn(
                    name
            );

            return subject;
        }

        ClassGrade classGrade(
                String code,
                String name
        ) {

            ClassGrade classGrade =
                    mock(
                            ClassGrade.class
                    );

            when(
                    classGrade.getClassCode()
            ).thenReturn(
                    code
            );

            when(
                    classGrade.getClassName()
            ).thenReturn(
                    name
            );

            return classGrade;
        }

        Stream stream(
                String code,
                String name
        ) {

            Stream stream =
                    mock(
                            Stream.class
                    );

            when(
                    stream.getStreamCode()
            ).thenReturn(
                    code
            );

            when(
                    stream.getStreamName()
            ).thenReturn(
                    name
            );

            return stream;
        }
    }
}
