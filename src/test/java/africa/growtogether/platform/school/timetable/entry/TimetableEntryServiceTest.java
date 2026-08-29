package africa.growtogether.platform.school.timetable.entry;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;

import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityEvaluator;
import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityConflict;

import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;

import africa.growtogether.platform.school.timetable.resource.SchedulingResource;
import africa.growtogether.platform.school.timetable.resource.SchedulingResourceRepository;

import africa.growtogether.platform.school.timetable.reliability.TimetableConflictService;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimetableEntryServiceTest {

    @Test
    void rejectsEntryOnDisabledBellScheduleDay() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                false
        );

        when(
                f.bellSchedule.isMondayEnabled()
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "MONDAY is not enabled on timetable bell schedule",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableEntry.class)
        );
    }

    @Test
    void rejectsSubjectOfferingFromDifferentClassOffering() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                false
        );

        when(
                f.subjectOffering.getClassOfferingId()
        ).thenReturn(
                UUID.randomUUID()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Subject offering does not belong to class offering",
                error.getMessage()
        );
    }

    @Test
    void rejectsTeachingAssignmentForDifferentSubject() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                false
        );

        when(
                f.assignment.getSubjectId()
        ).thenReturn(
                UUID.randomUUID()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Teaching assignment does not match subject",
                error.getMessage()
        );
    }

    @Test
    void rejectsClassWideDoubleBooking() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                false
        );

        TimetableEntry existing =
                mock(TimetableEntry.class);

        when(
                existing.getStreamId()
        ).thenReturn(null);

        when(
                f.repository
                        .findByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndClassGradeIdAndEntryStatusInAndStatus(
                                eq(f.tenantId),
                                eq(f.timetableId),
                                eq("MONDAY"),
                                eq(f.bellPeriodId),
                                eq(f.classGradeId),
                                anyCollection(),
                                eq(EntityStatus.ACTIVE)
                        )
        ).thenReturn(
                List.of(existing)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "S2-2026 is already scheduled for MONDAY P3",
                error.getMessage()
        );

        verify(
                f.conflicts
        ).recordDetectedConflict(
                eq(f.tenantId),
                eq(f.timetableId),
                isNull(),
                isNull(),
                eq("CLASS_DOUBLE_BOOKING"),
                eq("ERROR"),
                eq("S2-2026 is already scheduled for MONDAY P3"),
                eq("SYSTEM")
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableEntry.class)
        );
    }

    @Test
    void allowsDifferentStreamsInSameClassPeriod() {

        Fixture f = new Fixture();

        UUID streamOne =
                UUID.randomUUID();

        UUID streamTwo =
                UUID.randomUUID();

        f.stubHappyPath(
                streamOne,
                false
        );

        TimetableEntry existing =
                mock(TimetableEntry.class);

        when(
                existing.getStreamId()
        ).thenReturn(
                streamTwo
        );

        when(
                f.repository
                        .findByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndClassGradeIdAndEntryStatusInAndStatus(
                                eq(f.tenantId),
                                eq(f.timetableId),
                                eq("MONDAY"),
                                eq(f.bellPeriodId),
                                eq(f.classGradeId),
                                anyCollection(),
                                eq(EntityStatus.ACTIVE)
                        )
        ).thenReturn(
                List.of(existing)
        );

        when(
                f.repository
                        .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndTeacherProfileIdAndEntryStatusInAndStatus(
                                eq(f.tenantId),
                                eq(f.timetableId),
                                eq("MONDAY"),
                                eq(f.bellPeriodId),
                                eq(f.teacherId),
                                anyCollection(),
                                eq(EntityStatus.ACTIVE)
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                f.repository.save(
                        any(TimetableEntry.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        TimetableEntry result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                streamOne,
                                null
                        )
                );

        assertEquals(
                streamOne,
                result.getStreamId()
        );

        assertEquals(
                "SCHEDULED",
                result.getEntryStatus()
        );

        verify(
                f.repository
        ).save(
                any(TimetableEntry.class)
        );
    }

    @Test
    void rejectsTeacherDoubleBooking() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                false
        );

        when(
                f.repository
                        .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndTeacherProfileIdAndEntryStatusInAndStatus(
                                eq(f.tenantId),
                                eq(f.timetableId),
                                eq("MONDAY"),
                                eq(f.bellPeriodId),
                                eq(f.teacherId),
                                anyCollection(),
                                eq(EntityStatus.ACTIVE)
                        )
        ).thenReturn(
                Optional.of(
                        mock(TimetableEntry.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Teacher T-001 is already scheduled for MONDAY P3",
                error.getMessage()
        );

        verify(
                f.conflicts
        ).recordDetectedConflict(
                eq(f.tenantId),
                eq(f.timetableId),
                isNull(),
                isNull(),
                eq("TEACHER_DOUBLE_BOOKING"),
                eq("ERROR"),
                eq("Teacher T-001 is already scheduled for MONDAY P3"),
                eq("SYSTEM")
        );
    }

    @Test
    void rejectsSchedulingResourceDoubleBooking() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                true
        );

        when(
                f.repository
                        .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndSchedulingResourceIdAndEntryStatusInAndStatus(
                                eq(f.tenantId),
                                eq(f.timetableId),
                                eq("MONDAY"),
                                eq(f.bellPeriodId),
                                eq(f.resourceId),
                                anyCollection(),
                                eq(EntityStatus.ACTIVE)
                        )
        ).thenReturn(
                Optional.of(
                        mock(TimetableEntry.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        f.resourceId
                                )
                        )
                );

        assertEquals(
                "Science Laboratory is already booked for MONDAY P3",
                error.getMessage()
        );

        verify(
                f.conflicts
        ).recordDetectedConflict(
                eq(f.tenantId),
                eq(f.timetableId),
                isNull(),
                isNull(),
                eq("ROOM_DOUBLE_BOOKING"),
                eq("ERROR"),
                eq("Science Laboratory is already booked for MONDAY P3"),
                eq("SYSTEM")
        );
    }

    @Test
    void rejectsResourceSpecializedForDifferentSubject() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                true
        );

        when(
                f.resource.getSpecializedForSubjectId()
        ).thenReturn(
                UUID.randomUUID()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        f.resourceId
                                )
                        )
                );

        assertEquals(
                "Scheduling resource specialization does not match subject",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableEntry.class)
        );
    }


    @Test
    void rejectsTeacherWhenAvailabilityEvaluatorReportsUnavailable() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                false
        );

        when(
                f.availability.evaluate(
                        eq(f.tenantId),
                        eq(f.timetable),
                        eq(f.bellPeriod),
                        any(CreateTimetableEntryCommand.class)
                )
        ).thenReturn(
                Optional.of(
                        new TimetableAvailabilityConflict(
                                "TEACHER_UNAVAILABLE",
                                "Teacher is unavailable on 2026-08-24 during P3 (MEDICAL)"
                        )
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "Teacher is unavailable on 2026-08-24 during P3 (MEDICAL)",
                error.getMessage()
        );

        verify(
                f.conflicts
        ).recordDetectedConflict(
                eq(f.tenantId),
                eq(f.timetableId),
                isNull(),
                isNull(),
                eq("TEACHER_UNAVAILABLE"),
                eq("ERROR"),
                eq("Teacher is unavailable on 2026-08-24 during P3 (MEDICAL)"),
                eq("SYSTEM")
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableEntry.class)
        );
    }

    @Test
    void rejectsResourceWhenAvailabilityEvaluatorReportsUnavailable() {

        Fixture f = new Fixture();

        f.stubHappyPath(
                null,
                true
        );

        when(
                f.availability.evaluate(
                        eq(f.tenantId),
                        eq(f.timetable),
                        eq(f.bellPeriod),
                        any(CreateTimetableEntryCommand.class)
                )
        ).thenReturn(
                Optional.of(
                        new TimetableAvailabilityConflict(
                                "RESOURCE_UNAVAILABLE",
                                "Scheduling resource is unavailable on 2026-08-24 during P3 (MAINTENANCE)"
                        )
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        f.resourceId
                                )
                        )
                );

        assertEquals(
                "Scheduling resource is unavailable on 2026-08-24 during P3 (MAINTENANCE)",
                error.getMessage()
        );

        verify(
                f.conflicts
        ).recordDetectedConflict(
                eq(f.tenantId),
                eq(f.timetableId),
                isNull(),
                isNull(),
                eq("RESOURCE_UNAVAILABLE"),
                eq("ERROR"),
                eq("Scheduling resource is unavailable on 2026-08-24 during P3 (MAINTENANCE)"),
                eq("SYSTEM")
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableEntry.class)
        );
    }

    private static class Fixture {

        final TimetableEntryRepository repository =
                mock(TimetableEntryRepository.class);

        final TimetableRepository timetables =
                mock(TimetableRepository.class);

        final BellPeriodRepository bellPeriods =
                mock(BellPeriodRepository.class);

        final BellScheduleRepository bellSchedules =
                mock(BellScheduleRepository.class);

        final ClassOfferingRepository classOfferings =
                mock(ClassOfferingRepository.class);

        final SubjectOfferingRepository subjectOfferings =
                mock(SubjectOfferingRepository.class);

        final ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        final StreamRepository streams =
                mock(StreamRepository.class);

        final TeachingAssignmentRepository teachingAssignments =
                mock(TeachingAssignmentRepository.class);

        final TeacherProfileRepository teachers =
                mock(TeacherProfileRepository.class);

        final SchedulingResourceRepository resources =
                mock(SchedulingResourceRepository.class);

        final TimetableConflictService conflicts =
                mock(TimetableConflictService.class);

        final TimetableAvailabilityEvaluator availability =
                mock(TimetableAvailabilityEvaluator.class);

        final TimetableEntryService service =
                new TimetableEntryService(
                        repository,
                        timetables,
                        bellPeriods,
                        bellSchedules,
                        classOfferings,
                        subjectOfferings,
                        classGrades,
                        streams,
                        teachingAssignments,
                        teachers,
                        resources,
                        conflicts,
                        availability
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID timetableId =
                UUID.randomUUID();

        final UUID bellScheduleId =
                UUID.randomUUID();

        final UUID bellPeriodId =
                UUID.randomUUID();

        final UUID academicYearId =
                UUID.randomUUID();

        final UUID academicTermId =
                UUID.randomUUID();

        final UUID campusId =
                UUID.randomUUID();

        final UUID classOfferingId =
                UUID.randomUUID();

        final UUID subjectOfferingId =
                UUID.randomUUID();

        final UUID classGradeId =
                UUID.randomUUID();

        final UUID teachingAssignmentId =
                UUID.randomUUID();

        final UUID teacherId =
                UUID.randomUUID();

        final UUID subjectId =
                UUID.randomUUID();

        final UUID resourceId =
                UUID.randomUUID();

        final Timetable timetable =
                mock(Timetable.class);

        final BellPeriod bellPeriod =
                mock(BellPeriod.class);

        final BellSchedule bellSchedule =
                mock(BellSchedule.class);

        final ClassOffering classOffering =
                mock(ClassOffering.class);

        final SubjectOffering subjectOffering =
                mock(SubjectOffering.class);

        final TeachingAssignment assignment =
                mock(TeachingAssignment.class);

        final TeacherProfile teacher =
                mock(TeacherProfile.class);

        final SchedulingResource resource =
                mock(SchedulingResource.class);

        void stubHappyPath(
                UUID streamId,
                boolean withResource
        ) {

            when(
                    availability.evaluate(
                            eq(tenantId),
                            eq(timetable),
                            eq(bellPeriod),
                            any(CreateTimetableEntryCommand.class)
                    )
            ).thenReturn(
                    Optional.empty()
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
                    timetable.getTimetableStatus()
            ).thenReturn("DRAFT");

            when(
                    timetable.getBellScheduleId()
            ).thenReturn(bellScheduleId);

            when(
                    timetable.getAcademicYearId()
            ).thenReturn(academicYearId);

            when(
                    timetable.getAcademicTermId()
            ).thenReturn(academicTermId);

            when(
                    timetable.getCampusId()
            ).thenReturn(campusId);

            when(
                    timetable.getEffectiveFrom()
            ).thenReturn(
                    LocalDate.of(2026, 2, 1)
            );

            when(
                    timetable.getEffectiveTo()
            ).thenReturn(
                    LocalDate.of(2026, 4, 30)
            );

            when(
                    bellPeriods.findByTenantIdAndId(
                            tenantId,
                            bellPeriodId
                    )
            ).thenReturn(
                    Optional.of(bellPeriod)
            );

            when(
                    bellPeriod.getBellScheduleId()
            ).thenReturn(bellScheduleId);

            when(
                    bellPeriod.isSchedulingAllowed()
            ).thenReturn(true);

            when(
                    bellPeriod.getPeriodCode()
            ).thenReturn("P3");

            when(
                    bellSchedules.findByTenantIdAndId(
                            tenantId,
                            bellScheduleId
                    )
            ).thenReturn(
                    Optional.of(bellSchedule)
            );

            when(
                    bellSchedule.isMondayEnabled()
            ).thenReturn(true);

            when(
                    classOfferings.findByTenantIdAndId(
                            tenantId,
                            classOfferingId
                    )
            ).thenReturn(
                    Optional.of(classOffering)
            );

            when(
                    classOffering.getAcademicYearId()
            ).thenReturn(academicYearId);

            when(
                    classOffering.getCampusId()
            ).thenReturn(campusId);

            when(
                    classOffering.getClassGradeId()
            ).thenReturn(classGradeId);

            when(
                    classOffering.getOfferingCode()
            ).thenReturn("S2-2026");

            when(
                    subjectOfferings.findByTenantIdAndId(
                            tenantId,
                            subjectOfferingId
                    )
            ).thenReturn(
                    Optional.of(subjectOffering)
            );

            when(
                    subjectOffering.getClassOfferingId()
            ).thenReturn(classOfferingId);

            when(
                    subjectOffering.getAcademicTermId()
            ).thenReturn(academicTermId);

            when(
                    subjectOffering.getStreamId()
            ).thenReturn(streamId);

            when(
                    subjectOffering.getSubjectId()
            ).thenReturn(subjectId);

            when(
                    classGrades.findByTenantIdAndId(
                            tenantId,
                            classGradeId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(ClassGrade.class)
                    )
            );

            if (streamId != null) {

                Stream stream =
                        mock(Stream.class);

                when(
                        stream.getCampusId()
                ).thenReturn(campusId);

                when(
                        stream.getClassGradeId()
                ).thenReturn(classGradeId);

                when(
                        streams.findByTenantIdAndId(
                                tenantId,
                                streamId
                        )
                ).thenReturn(
                        Optional.of(stream)
                );
            }

            when(
                    teachingAssignments.findByTenantIdAndId(
                            tenantId,
                            teachingAssignmentId
                    )
            ).thenReturn(
                    Optional.of(assignment)
            );

            when(
                    assignment.getAssignmentStatus()
            ).thenReturn("ACTIVE");

            when(
                    assignment.getAcademicYearId()
            ).thenReturn(academicYearId);

            when(
                    assignment.getAcademicTermId()
            ).thenReturn(academicTermId);

            when(
                    assignment.getCampusId()
            ).thenReturn(campusId);

            when(
                    assignment.getClassGradeId()
            ).thenReturn(classGradeId);

            when(
                    assignment.getStreamId()
            ).thenReturn(streamId);

            when(
                    assignment.getSubjectId()
            ).thenReturn(subjectId);

            when(
                    assignment.getTeacherProfileId()
            ).thenReturn(teacherId);

            when(
                    assignment.getEffectiveFrom()
            ).thenReturn(
                    LocalDate.of(2026, 2, 1)
            );

            when(
                    assignment.getEffectiveTo()
            ).thenReturn(
                    LocalDate.of(2026, 4, 30)
            );

            when(
                    teachers.findByTenantIdAndId(
                            tenantId,
                            teacherId
                    )
            ).thenReturn(
                    Optional.of(teacher)
            );

            when(
                    teacher.getTeachingStatus()
            ).thenReturn("ACTIVE");

            when(
                    teacher.getTeacherNumber()
            ).thenReturn("T-001");

            when(
                    repository
                            .findByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndClassGradeIdAndEntryStatusInAndStatus(
                                    eq(tenantId),
                                    eq(timetableId),
                                    eq("MONDAY"),
                                    eq(bellPeriodId),
                                    eq(classGradeId),
                                    anyCollection(),
                                    eq(EntityStatus.ACTIVE)
                            )
            ).thenReturn(
                    List.of()
            );

            when(
                    repository
                            .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndTeacherProfileIdAndEntryStatusInAndStatus(
                                    eq(tenantId),
                                    eq(timetableId),
                                    eq("MONDAY"),
                                    eq(bellPeriodId),
                                    eq(teacherId),
                                    anyCollection(),
                                    eq(EntityStatus.ACTIVE)
                            )
            ).thenReturn(
                    Optional.empty()
            );

            if (withResource) {

                when(
                        resources.findByTenantIdAndId(
                                tenantId,
                                resourceId
                        )
                ).thenReturn(
                        Optional.of(resource)
                );

                when(
                        resource.getCampusId()
                ).thenReturn(campusId);

                when(
                        resource.isBookable()
                ).thenReturn(true);

                when(
                        resource.getResourceStatus()
                ).thenReturn("ACTIVE");

                when(
                        resource.getSpecializedForSubjectId()
                ).thenReturn(subjectId);

                when(
                        resource.getResourceName()
                ).thenReturn("Science Laboratory");

                when(
                        repository
                                .findFirstByTenantIdAndTimetableIdAndDayOfWeekAndBellPeriodIdAndSchedulingResourceIdAndEntryStatusInAndStatus(
                                        eq(tenantId),
                                        eq(timetableId),
                                        eq("MONDAY"),
                                        eq(bellPeriodId),
                                        eq(resourceId),
                                        anyCollection(),
                                        eq(EntityStatus.ACTIVE)
                                )
                ).thenReturn(
                        Optional.empty()
                );
            }

            when(
                    repository.save(
                            any(TimetableEntry.class)
                    )
            ).thenAnswer(
                    invocation ->
                            invocation.getArgument(0)
            );
        }

        CreateTimetableEntryCommand command(
                UUID streamId,
                UUID schedulingResourceId
        ) {

            return new CreateTimetableEntryCommand(
                    timetableId,
                    bellPeriodId,
                    "MONDAY",
                    classOfferingId,
                    subjectOfferingId,
                    classGradeId,
                    streamId,
                    teachingAssignmentId,
                    teacherId,
                    schedulingResourceId,
                    "LESSON",
                    null,
                    "Regular Mathematics lesson",
                    true,
                    null,
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 4, 30)
            );
        }
    }
}
