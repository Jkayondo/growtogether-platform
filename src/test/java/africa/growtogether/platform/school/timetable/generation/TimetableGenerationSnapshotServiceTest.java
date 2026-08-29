package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;

import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;

import africa.growtogether.platform.school.timetable.resource.SchedulingResource;
import africa.growtogether.platform.school.timetable.resource.SchedulingResourceRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimetableGenerationSnapshotServiceTest {

    @Test
    void buildsControlledSnapshotWithWeeklyDemandAndTeachingSupply() {

        Fixture f = new Fixture();

        f.stubBase();

        UUID classOfferingId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID subjectOfferingId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID streamId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        UUID assignmentId =
                UUID.randomUUID();

        UUID resourceId =
                UUID.randomUUID();

        BellPeriod lesson =
                mock(BellPeriod.class);

        when(lesson.getId())
                .thenReturn(UUID.randomUUID());

        when(lesson.getPeriodCode())
                .thenReturn("P1");

        when(lesson.getSequenceNumber())
                .thenReturn(1);

        when(lesson.getPeriodType())
                .thenReturn("TEACHING");

        when(lesson.getStartTime())
                .thenReturn(LocalTime.of(8, 0));

        when(lesson.getEndTime())
                .thenReturn(LocalTime.of(8, 40));

        when(lesson.isSchedulingAllowed())
                .thenReturn(true);

        when(
                f.bellPeriods
                        .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                f.tenantId,
                                f.bellScheduleId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(lesson)
        );

        ClassOffering classOffering =
                mock(ClassOffering.class);

        when(classOffering.getId())
                .thenReturn(classOfferingId);

        when(classOffering.getOfferingCode())
                .thenReturn("P7-2026");

        when(classOffering.getAcademicYearId())
                .thenReturn(f.academicYearId);

        when(classOffering.getCampusId())
                .thenReturn(f.campusId);

        when(classOffering.getClassGradeId())
                .thenReturn(classGradeId);

        when(classOffering.getOfferingStatus())
                .thenReturn("ACTIVE");

        when(
                f.classOfferings
                        .findByTenantIdAndAcademicYearId(
                                f.tenantId,
                                f.academicYearId
                        )
        ).thenReturn(
                List.of(classOffering)
        );

        SubjectOffering subject =
                mock(SubjectOffering.class);

        when(subject.getId())
                .thenReturn(subjectOfferingId);

        when(subject.getSubjectOfferingCode())
                .thenReturn("MATH-P7-2026");

        when(subject.getClassOfferingId())
                .thenReturn(classOfferingId);

        when(subject.getAcademicTermId())
                .thenReturn(f.academicTermId);

        when(subject.getStreamId())
                .thenReturn(streamId);

        when(subject.getSubjectId())
                .thenReturn(subjectId);

        when(subject.getWeeklyPeriods())
                .thenReturn(7);

        when(subject.getOfferingStatus())
                .thenReturn("ACTIVE");

        when(
                f.subjectOfferings
                        .findByTenantIdAndClassOfferingId(
                                f.tenantId,
                                classOfferingId
                        )
        ).thenReturn(
                List.of(subject)
        );

        Stream stream =
                mock(Stream.class);

        when(stream.getId())
                .thenReturn(streamId);

        when(stream.getCampusId())
                .thenReturn(f.campusId);

        when(stream.getClassGradeId())
                .thenReturn(classGradeId);

        when(
                f.streams.findByTenantIdAndCampusId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                List.of(stream)
        );

        TeachingAssignment assignment =
                mock(TeachingAssignment.class);

        when(assignment.getId())
                .thenReturn(assignmentId);

        when(assignment.getAssignmentReference())
                .thenReturn("TA-P7-MATH");

        when(assignment.getTeacherProfileId())
                .thenReturn(teacherId);

        when(assignment.getAcademicYearId())
                .thenReturn(f.academicYearId);

        when(assignment.getAcademicTermId())
                .thenReturn(f.academicTermId);

        when(assignment.getCampusId())
                .thenReturn(f.campusId);

        when(assignment.getClassGradeId())
                .thenReturn(classGradeId);

        when(assignment.getStreamId())
                .thenReturn(streamId);

        when(assignment.getSubjectId())
                .thenReturn(subjectId);

        when(assignment.getWeeklyPeriods())
                .thenReturn(7);

        when(assignment.getWorkloadPercentage())
                .thenReturn(new BigDecimal("75.00"));

        when(assignment.getEffectiveFrom())
                .thenReturn(LocalDate.of(2026, 2, 1));

        when(assignment.getEffectiveTo())
                .thenReturn(LocalDate.of(2026, 4, 30));

        when(assignment.getAssignmentStatus())
                .thenReturn("ACTIVE");

        when(
                f.teachingAssignments
                        .findByTenantIdAndAcademicYearId(
                                f.tenantId,
                                f.academicYearId
                        )
        ).thenReturn(
                List.of(assignment)
        );

        SchedulingResource resource =
                mock(SchedulingResource.class);

        when(resource.getId())
                .thenReturn(resourceId);

        when(resource.getResourceCode())
                .thenReturn("ROOM-P7");

        when(resource.getResourceName())
                .thenReturn("P7 Classroom");

        when(resource.getResourceType())
                .thenReturn("CLASSROOM");

        when(resource.getCapacity())
                .thenReturn(45);

        when(resource.isBookable())
                .thenReturn(true);

        when(resource.getResourceStatus())
                .thenReturn("ACTIVE");

        when(
                f.resources.findByTenantIdAndCampusId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                List.of(resource)
        );

        TimetableGenerationSnapshot result =
                f.service.build(
                        f.tenantId,
                        f.requestId
                );

        assertEquals(
                "Africa/Kampala",
                result.scope().schoolTimezone()
        );

        assertEquals(
                List.of(
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY"
                ),
                result.enabledDays()
        );

        assertEquals(
                1,
                result.bellSlots().size()
        );

        assertEquals(
                1,
                result.classes().size()
        );

        assertEquals(
                1,
                result.subjects().size()
        );

        assertEquals(
                7,
                result.subjects()
                        .get(0)
                        .weeklyPeriods()
        );

        assertEquals(
                subjectId,
                result.subjects()
                        .get(0)
                        .subjectId()
        );

        assertEquals(
                1,
                result.teachingAssignments().size()
        );

        assertEquals(
                7,
                result.teachingAssignments()
                        .get(0)
                        .weeklyPeriods()
        );

        assertEquals(
                new BigDecimal("75.00"),
                result.teachingAssignments()
                        .get(0)
                        .workloadPercentage()
        );

        assertEquals(
                teacherId,
                result.teachingAssignments()
                        .get(0)
                        .teacherProfileId()
        );

        assertEquals(
                1,
                result.resources().size()
        );

        assertEquals(
                "ROOM-P7",
                result.resources()
                        .get(0)
                        .resourceCode()
        );
    }

    @Test
    void excludesNonSchedulingPeriodsAndUnbookableOrInactiveResources() {

        Fixture f = new Fixture();

        f.stubBase();

        BellPeriod teaching =
                mock(BellPeriod.class);

        when(teaching.getId())
                .thenReturn(UUID.randomUUID());

        when(teaching.getPeriodCode())
                .thenReturn("P1");

        when(teaching.getSequenceNumber())
                .thenReturn(1);

        when(teaching.getPeriodType())
                .thenReturn("TEACHING");

        when(teaching.getStartTime())
                .thenReturn(LocalTime.of(8, 0));

        when(teaching.getEndTime())
                .thenReturn(LocalTime.of(8, 40));

        when(teaching.isSchedulingAllowed())
                .thenReturn(true);

        BellPeriod breakPeriod =
                mock(BellPeriod.class);

        when(breakPeriod.isSchedulingAllowed())
                .thenReturn(false);

        when(
                f.bellPeriods
                        .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                f.tenantId,
                                f.bellScheduleId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(
                        teaching,
                        breakPeriod
                )
        );

        SchedulingResource valid =
                f.resource(
                        "LAB-1",
                        "ACTIVE",
                        true
                );

        SchedulingResource unbookable =
                f.resource(
                        "HALL-1",
                        "ACTIVE",
                        false
                );

        SchedulingResource inactive =
                f.resource(
                        "ROOM-OLD",
                        "INACTIVE",
                        true
                );

        when(
                f.resources.findByTenantIdAndCampusId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                List.of(
                        valid,
                        unbookable,
                        inactive
                )
        );

        TimetableGenerationSnapshot result =
                f.service.build(
                        f.tenantId,
                        f.requestId
                );

        assertEquals(
                1,
                result.bellSlots().size()
        );

        assertEquals(
                "P1",
                result.bellSlots()
                        .get(0)
                        .periodCode()
        );

        assertEquals(
                1,
                result.resources().size()
        );

        assertEquals(
                "LAB-1",
                result.resources()
                        .get(0)
                        .resourceCode()
        );
    }

    @Test
    void includesSaturdayAndSundayOnlyWhenBellScheduleEnablesThem() {

        Fixture f = new Fixture();

        f.stubBase();

        TimetableGenerationSnapshot weekdayOnly =
                f.service.build(
                        f.tenantId,
                        f.requestId
                );

        assertFalse(
                weekdayOnly.enabledDays()
                        .contains("SATURDAY")
        );

        assertFalse(
                weekdayOnly.enabledDays()
                        .contains("SUNDAY")
        );

        when(
                f.bellSchedule.isSaturdayEnabled()
        ).thenReturn(true);

        when(
                f.bellSchedule.isSundayEnabled()
        ).thenReturn(true);

        TimetableGenerationSnapshot weekendEnabled =
                f.service.build(
                        f.tenantId,
                        f.requestId
                );

        assertTrue(
                weekendEnabled.enabledDays()
                        .contains("SATURDAY")
        );

        assertTrue(
                weekendEnabled.enabledDays()
                        .contains("SUNDAY")
        );
    }

    @Test
    void filtersClassesSubjectsAndAssignmentsOutsideGenerationScope() {

        Fixture f = new Fixture();

        f.stubBase();

        UUID validGradeId =
                UUID.randomUUID();

        UUID validClassId =
                UUID.randomUUID();

        ClassOffering validClass =
                f.classOffering(
                        validClassId,
                        "P6",
                        f.campusId,
                        validGradeId,
                        "ACTIVE"
                );

        ClassOffering wrongCampus =
                f.classOffering(
                        UUID.randomUUID(),
                        "P5",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "ACTIVE"
                );

        ClassOffering inactiveClass =
                f.classOffering(
                        UUID.randomUUID(),
                        "P4",
                        f.campusId,
                        UUID.randomUUID(),
                        "INACTIVE"
                );

        when(
                f.classOfferings
                        .findByTenantIdAndAcademicYearId(
                                f.tenantId,
                                f.academicYearId
                        )
        ).thenReturn(
                List.of(
                        validClass,
                        wrongCampus,
                        inactiveClass
                )
        );

        SubjectOffering validSubject =
                f.subject(
                        "ENG-P6",
                        validClassId,
                        f.academicTermId,
                        "ACTIVE"
                );

        SubjectOffering wrongTerm =
                f.subject(
                        "SCI-P6",
                        validClassId,
                        UUID.randomUUID(),
                        "ACTIVE"
                );

        SubjectOffering inactiveSubject =
                f.subject(
                        "SST-P6",
                        validClassId,
                        f.academicTermId,
                        "INACTIVE"
                );

        when(
                f.subjectOfferings
                        .findByTenantIdAndClassOfferingId(
                                f.tenantId,
                                validClassId
                        )
        ).thenReturn(
                List.of(
                        validSubject,
                        wrongTerm,
                        inactiveSubject
                )
        );

        TeachingAssignment validAssignment =
                f.assignment(
                        "TA-VALID",
                        f.campusId,
                        f.academicTermId,
                        "ACTIVE",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 4, 30)
                );

        TeachingAssignment wrongCampusAssignment =
                f.assignment(
                        "TA-WRONG-CAMPUS",
                        UUID.randomUUID(),
                        f.academicTermId,
                        "ACTIVE",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 4, 30)
                );

        TeachingAssignment expiredAssignment =
                f.assignment(
                        "TA-EXPIRED",
                        f.campusId,
                        f.academicTermId,
                        "ACTIVE",
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 12, 31)
                );

        when(
                f.teachingAssignments
                        .findByTenantIdAndAcademicYearId(
                                f.tenantId,
                                f.academicYearId
                        )
        ).thenReturn(
                List.of(
                        validAssignment,
                        wrongCampusAssignment,
                        expiredAssignment
                )
        );

        TimetableGenerationSnapshot result =
                f.service.build(
                        f.tenantId,
                        f.requestId
                );

        assertEquals(
                1,
                result.classes().size()
        );

        assertEquals(
                "P6",
                result.classes()
                        .get(0)
                        .offeringCode()
        );

        assertEquals(
                1,
                result.subjects().size()
        );

        assertEquals(
                "ENG-P6",
                result.subjects()
                        .get(0)
                        .subjectOfferingCode()
        );

        assertEquals(
                1,
                result.teachingAssignments().size()
        );

        assertEquals(
                "TA-VALID",
                result.teachingAssignments()
                        .get(0)
                        .assignmentReference()
        );
    }

    @Test
    void rejectsNonReadyGenerationRequestBeforeReadingSchedulingInputs() {

        Fixture f = new Fixture();

        f.stubBase();

        when(
                f.request.getGenerationStatus()
        ).thenReturn(
                "DRAFT"
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> f.service.build(
                                f.tenantId,
                                f.requestId
                        )
                );

        assertEquals(
                "Generation snapshot can only be built for a READY request",
                error.getMessage()
        );

        verify(
                f.bellSchedules,
                never()
        ).findByTenantIdAndId(
                any(UUID.class),
                any(UUID.class)
        );

        verifyNoInteractions(
                f.bellPeriods
        );

        verifyNoInteractions(
                f.classOfferings
        );

        verifyNoInteractions(
                f.subjectOfferings
        );

        verifyNoInteractions(
                f.teachingAssignments
        );

        verifyNoInteractions(
                f.resources
        );
    }

    private static class Fixture {

        final TimetableGenerationRequestRepository generationRequests =
                mock(TimetableGenerationRequestRepository.class);

        final BellScheduleRepository bellSchedules =
                mock(BellScheduleRepository.class);

        final BellPeriodRepository bellPeriods =
                mock(BellPeriodRepository.class);

        final ClassOfferingRepository classOfferings =
                mock(ClassOfferingRepository.class);

        final SubjectOfferingRepository subjectOfferings =
                mock(SubjectOfferingRepository.class);

        final StreamRepository streams =
                mock(StreamRepository.class);

        final TeachingAssignmentRepository teachingAssignments =
                mock(TeachingAssignmentRepository.class);

        final SchedulingResourceRepository resources =
                mock(SchedulingResourceRepository.class);

        final SchoolProfileService schoolProfiles =
                mock(SchoolProfileService.class);

        final TimetableGenerationSnapshotService service =
                new TimetableGenerationSnapshotService(
                        generationRequests,
                        bellSchedules,
                        bellPeriods,
                        classOfferings,
                        subjectOfferings,
                        streams,
                        teachingAssignments,
                        resources,
                        schoolProfiles
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID requestId =
                UUID.randomUUID();

        final UUID academicYearId =
                UUID.randomUUID();

        final UUID academicTermId =
                UUID.randomUUID();

        final UUID campusId =
                UUID.randomUUID();

        final UUID bellScheduleId =
                UUID.randomUUID();

        final TimetableGenerationRequest request =
                mock(TimetableGenerationRequest.class);

        final BellSchedule bellSchedule =
                mock(BellSchedule.class);

        void stubBase() {

            when(
                    generationRequests.findByTenantIdAndId(
                            tenantId,
                            requestId
                    )
            ).thenReturn(
                    Optional.of(request)
            );

            when(request.getId())
                    .thenReturn(requestId);

            when(request.getGenerationCode())
                    .thenReturn("GEN-2026-T1");

            when(request.getGenerationStatus())
                    .thenReturn("READY");

            when(request.getAcademicYearId())
                    .thenReturn(academicYearId);

            when(request.getAcademicTermId())
                    .thenReturn(academicTermId);

            when(request.getCampusId())
                    .thenReturn(campusId);

            when(request.getBellScheduleId())
                    .thenReturn(bellScheduleId);

            when(request.getTimetableType())
                    .thenReturn("MASTER");

            when(request.getEffectiveFrom())
                    .thenReturn(
                            LocalDate.of(
                                    2026,
                                    2,
                                    2
                            )
                    );

            when(request.getEffectiveTo())
                    .thenReturn(
                            LocalDate.of(
                                    2026,
                                    4,
                                    30
                            )
                    );

            when(request.getGenerationMode())
                    .thenReturn("AI_ASSISTED");

            when(request.getModelCode())
                    .thenReturn("GT-TIMETABLE-AI");

            when(request.getObjectives())
                    .thenReturn(
                            "Prefer core subjects in the morning"
                    );

            when(
                    bellSchedules.findByTenantIdAndId(
                            tenantId,
                            bellScheduleId
                    )
            ).thenReturn(
                    Optional.of(bellSchedule)
            );

            when(bellSchedule.getCampusId())
                    .thenReturn(campusId);

            when(bellSchedule.isMondayEnabled())
                    .thenReturn(true);

            when(bellSchedule.isTuesdayEnabled())
                    .thenReturn(true);

            when(bellSchedule.isWednesdayEnabled())
                    .thenReturn(true);

            when(bellSchedule.isThursdayEnabled())
                    .thenReturn(true);

            when(bellSchedule.isFridayEnabled())
                    .thenReturn(true);

            when(bellSchedule.isSaturdayEnabled())
                    .thenReturn(false);

            when(bellSchedule.isSundayEnabled())
                    .thenReturn(false);

            when(
                    schoolProfiles.requireTimezone(
                            tenantId
                    )
            ).thenReturn(
                    ZoneId.of(
                            "Africa/Kampala"
                    )
            );

            when(
                    bellPeriods
                            .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                    tenantId,
                                    bellScheduleId,
                                    EntityStatus.ACTIVE
                            )
            ).thenReturn(
                    List.of()
            );

            when(
                    classOfferings
                            .findByTenantIdAndAcademicYearId(
                                    tenantId,
                                    academicYearId
                            )
            ).thenReturn(
                    List.of()
            );

            when(
                    streams.findByTenantIdAndCampusId(
                            tenantId,
                            campusId
                    )
            ).thenReturn(
                    List.of()
            );

            when(
                    teachingAssignments
                            .findByTenantIdAndAcademicYearId(
                                    tenantId,
                                    academicYearId
                            )
            ).thenReturn(
                    List.of()
            );

            when(
                    resources.findByTenantIdAndCampusId(
                            tenantId,
                            campusId
                    )
            ).thenReturn(
                    List.of()
            );
        }

        SchedulingResource resource(
                String code,
                String status,
                boolean bookable
        ) {

            SchedulingResource item =
                    mock(SchedulingResource.class);

            when(item.getId())
                    .thenReturn(UUID.randomUUID());

            when(item.getResourceCode())
                    .thenReturn(code);

            when(item.getResourceName())
                    .thenReturn(code);

            when(item.getResourceType())
                    .thenReturn("CLASSROOM");

            when(item.getCapacity())
                    .thenReturn(40);

            when(item.getResourceStatus())
                    .thenReturn(status);

            when(item.isBookable())
                    .thenReturn(bookable);

            return item;
        }

        ClassOffering classOffering(
                UUID id,
                String code,
                UUID campus,
                UUID grade,
                String status
        ) {

            ClassOffering item =
                    mock(ClassOffering.class);

            when(item.getId())
                    .thenReturn(id);

            when(item.getOfferingCode())
                    .thenReturn(code);

            when(item.getAcademicYearId())
                    .thenReturn(academicYearId);

            when(item.getCampusId())
                    .thenReturn(campus);

            when(item.getClassGradeId())
                    .thenReturn(grade);

            when(item.getOfferingStatus())
                    .thenReturn(status);

            return item;
        }

        SubjectOffering subject(
                String code,
                UUID classOfferingId,
                UUID termId,
                String status
        ) {

            SubjectOffering item =
                    mock(SubjectOffering.class);

            when(item.getId())
                    .thenReturn(UUID.randomUUID());

            when(item.getSubjectOfferingCode())
                    .thenReturn(code);

            when(item.getClassOfferingId())
                    .thenReturn(classOfferingId);

            when(item.getAcademicTermId())
                    .thenReturn(termId);

            when(item.getStreamId())
                    .thenReturn(null);

            when(item.getSubjectId())
                    .thenReturn(UUID.randomUUID());

            when(item.getWeeklyPeriods())
                    .thenReturn(5);

            when(item.getOfferingStatus())
                    .thenReturn(status);

            return item;
        }

        TeachingAssignment assignment(
                String reference,
                UUID campus,
                UUID term,
                String status,
                LocalDate effectiveFrom,
                LocalDate effectiveTo
        ) {

            TeachingAssignment item =
                    mock(TeachingAssignment.class);

            when(item.getId())
                    .thenReturn(UUID.randomUUID());

            when(item.getAssignmentReference())
                    .thenReturn(reference);

            when(item.getTeacherProfileId())
                    .thenReturn(UUID.randomUUID());

            when(item.getAcademicYearId())
                    .thenReturn(academicYearId);

            when(item.getAcademicTermId())
                    .thenReturn(term);

            when(item.getCampusId())
                    .thenReturn(campus);

            when(item.getClassGradeId())
                    .thenReturn(UUID.randomUUID());

            when(item.getStreamId())
                    .thenReturn(null);

            when(item.getSubjectId())
                    .thenReturn(UUID.randomUUID());

            when(item.getWeeklyPeriods())
                    .thenReturn(5);

            when(item.getWorkloadPercentage())
                    .thenReturn(new BigDecimal("50.00"));

            when(item.getEffectiveFrom())
                    .thenReturn(effectiveFrom);

            when(item.getEffectiveTo())
                    .thenReturn(effectiveTo);

            when(item.getAssignmentStatus())
                    .thenReturn(status);

            return item;
        }
    }
}
