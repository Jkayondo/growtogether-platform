package africa.growtogether.platform.school.timetable.generation;

import org.junit.jupiter.api.Test;

import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityConflict;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DeterministicTimetableCandidateGeneratorTest {

    private final UUID tenantId =
            UUID.randomUUID();

    private final TimetableCandidateAvailabilityService candidateAvailability =
            mock(TimetableCandidateAvailabilityService.class);

    private final DeterministicTimetableCandidateGenerator generator =
            new DeterministicTimetableCandidateGenerator(
                    candidateAvailability
            );

    @Test
    void fulfilsExactWeeklyDemandAndDistributesAcrossEnabledDays() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID subjectOfferingId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        List.of(
                                "MONDAY",
                                "TUESDAY",
                                "WEDNESDAY",
                                "THURSDAY",
                                "FRIDAY"
                        ),
                        List.of(
                                slot("P1", 1)
                        ),
                        List.of(
                                classDemand(
                                        classId,
                                        "P7",
                                        gradeId
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        subjectOfferingId,
                                        "MATH-P7",
                                        classId,
                                        null,
                                        subjectId,
                                        5
                                )
                        ),
                        List.of(
                                assignment(
                                        assignmentId,
                                        "TA-MATH-P7",
                                        teacherId,
                                        gradeId,
                                        null,
                                        subjectId,
                                        5
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(tenantId, snapshot);

        assertEquals("COMPLETE", result.candidateStatus());
        assertEquals(5, result.requiredPeriods());
        assertEquals(5, result.placedPeriods());
        assertTrue(result.unplaced().isEmpty());

        Set<String> scheduledDays =
                result.placements()
                        .stream()
                        .map(
                                TimetableGenerationCandidate.Placement
                                        ::dayOfWeek
                        )
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY"
                ),
                scheduledDays
        );

        assertTrue(
                result.placements()
                        .stream()
                        .allMatch(
                                placement ->
                                        placement.subjectOfferingId()
                                                .equals(subjectOfferingId)
                                        && placement.teacherProfileId()
                                                .equals(teacherId)
                        )
        );
    }

    @Test
    void preventsTeacherDoubleBookingAcrossDifferentClasses() {

        UUID grade1 = UUID.randomUUID();
        UUID grade2 = UUID.randomUUID();

        UUID class1 = UUID.randomUUID();
        UUID class2 = UUID.randomUUID();

        UUID subject1 = UUID.randomUUID();
        UUID subject2 = UUID.randomUUID();

        UUID teacherId = UUID.randomUUID();

        TimetableGenerationSnapshot.BellSlot p1 =
                slot("P1", 1);

        TimetableGenerationSnapshot.BellSlot p2 =
                slot("P2", 2);

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        List.of("MONDAY"),
                        List.of(p1, p2),
                        List.of(
                                classDemand(
                                        class1,
                                        "P6",
                                        grade1
                                ),
                                classDemand(
                                        class2,
                                        "P7",
                                        grade2
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "A-MATH-P6",
                                        class1,
                                        null,
                                        subject1,
                                        1
                                ),
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "B-MATH-P7",
                                        class2,
                                        null,
                                        subject2,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-01",
                                        teacherId,
                                        grade1,
                                        null,
                                        subject1,
                                        1
                                ),
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-02",
                                        teacherId,
                                        grade2,
                                        null,
                                        subject2,
                                        1
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(tenantId, snapshot);

        assertEquals("COMPLETE", result.candidateStatus());
        assertEquals(2, result.placedPeriods());

        Set<UUID> teacherSlots =
                result.placements()
                        .stream()
                        .filter(
                                placement ->
                                        placement.teacherProfileId()
                                                .equals(teacherId)
                        )
                        .map(
                                TimetableGenerationCandidate.Placement
                                        ::bellPeriodId
                        )
                        .collect(Collectors.toSet());

        assertEquals(
                2,
                teacherSlots.size()
        );
    }

    @Test
    void preventsSameStreamCollisionButAllowsDifferentStreamsAtSamePeriod() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        UUID streamA = UUID.randomUUID();
        UUID streamB = UUID.randomUUID();

        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        UUID subjectC = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        List.of("MONDAY"),
                        List.of(
                                slot("P1", 1)
                        ),
                        List.of(
                                classDemand(
                                        classId,
                                        "P7",
                                        gradeId
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "A-FIRST",
                                        classId,
                                        streamA,
                                        subjectA,
                                        1
                                ),
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "B-SECOND",
                                        classId,
                                        streamA,
                                        subjectB,
                                        1
                                ),
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "C-THIRD",
                                        classId,
                                        streamB,
                                        subjectC,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-A",
                                        UUID.randomUUID(),
                                        gradeId,
                                        streamA,
                                        subjectA,
                                        1
                                ),
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-B",
                                        UUID.randomUUID(),
                                        gradeId,
                                        streamA,
                                        subjectB,
                                        1
                                ),
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-C",
                                        UUID.randomUUID(),
                                        gradeId,
                                        streamB,
                                        subjectC,
                                        1
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(tenantId, snapshot);

        assertEquals("INCOMPLETE", result.candidateStatus());
        assertEquals(3, result.requiredPeriods());
        assertEquals(2, result.placedPeriods());
        assertEquals(1, result.unplaced().size());

        assertEquals(
                "B-SECOND",
                result.unplaced()
                        .get(0)
                        .subjectOfferingCode()
        );

        assertEquals(
                "NO_CONFLICT_FREE_SLOT",
                result.unplaced()
                        .get(0)
                        .reasonCode()
        );

        Set<UUID> placedStreams =
                result.placements()
                        .stream()
                        .map(
                                TimetableGenerationCandidate.Placement
                                        ::streamId
                        )
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        streamA,
                        streamB
                ),
                placedStreams
        );
    }

    @Test
    void respectsTeachingAssignmentWeeklyCapacity() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        List.of(
                                "MONDAY",
                                "TUESDAY",
                                "WEDNESDAY"
                        ),
                        List.of(
                                slot("P1", 1)
                        ),
                        List.of(
                                classDemand(
                                        classId,
                                        "P5",
                                        gradeId
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "SCI-P5",
                                        classId,
                                        null,
                                        subjectId,
                                        3
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-SCI-P5",
                                        UUID.randomUUID(),
                                        gradeId,
                                        null,
                                        subjectId,
                                        2
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(tenantId, snapshot);

        assertEquals("INCOMPLETE", result.candidateStatus());
        assertEquals(3, result.requiredPeriods());
        assertEquals(2, result.placedPeriods());
        assertEquals(1, result.unplaced().size());

        assertEquals(
                1,
                result.unplaced()
                        .get(0)
                        .remainingPeriods()
        );

        assertEquals(
                "NO_CONFLICT_FREE_SLOT",
                result.unplaced()
                        .get(0)
                        .reasonCode()
        );
    }

    @Test
    void reportsExplicitEvidenceWhenNoTeachingAssignmentMatches() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        List.of(
                                "MONDAY",
                                "TUESDAY"
                        ),
                        List.of(
                                slot("P1", 1)
                        ),
                        List.of(
                                classDemand(
                                        classId,
                                        "P4",
                                        gradeId
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "ENG-P4",
                                        classId,
                                        null,
                                        subjectId,
                                        2
                                )
                        ),
                        List.of()
                );

        TimetableGenerationCandidate result =
                generator.generate(tenantId, snapshot);

        assertEquals("INCOMPLETE", result.candidateStatus());
        assertEquals(2, result.requiredPeriods());
        assertEquals(0, result.placedPeriods());

        assertEquals(
                1,
                result.unplaced().size()
        );

        TimetableGenerationCandidate.UnplacedDemand failure =
                result.unplaced().get(0);

        assertEquals(
                "ENG-P4",
                failure.subjectOfferingCode()
        );

        assertEquals(
                2,
                failure.remainingPeriods()
        );

        assertEquals(
                "NO_MATCHING_TEACHING_ASSIGNMENT",
                failure.reasonCode()
        );

        assertFalse(
                failure.explanation().isBlank()
        );
    }

    @Test
    void skipsUnavailableTeacherSlotAndSelectsNextValidPeriod() {

        UUID gradeId =
                UUID.randomUUID();

        UUID classId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID subjectOfferingId =
                UUID.randomUUID();

        UUID teacherId =
                UUID.randomUUID();

        UUID assignmentId =
                UUID.randomUUID();

        TimetableGenerationSnapshot.BellSlot p1 =
                slot(
                        "P1",
                        1
                );

        TimetableGenerationSnapshot.BellSlot p2 =
                slot(
                        "P2",
                        2
                );

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        List.of(
                                "MONDAY"
                        ),
                        List.of(
                                p1,
                                p2
                        ),
                        List.of(
                                classDemand(
                                        classId,
                                        "P7",
                                        gradeId
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        subjectOfferingId,
                                        "MATH-P7",
                                        classId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        assignmentId,
                                        "TA-MATH-P7",
                                        teacherId,
                                        gradeId,
                                        null,
                                        subjectId,
                                        1
                                )
                        )
                );

        org.mockito.Mockito.when(
                candidateAvailability.evaluate(
                        tenantId,
                        snapshot,
                        p1,
                        "MONDAY",
                        teacherId,
                        null
                )
        ).thenReturn(
                java.util.Optional.of(
                        new TimetableAvailabilityConflict(
                                "TEACHER_UNAVAILABLE",
                                "Teacher is unavailable during P1"
                        )
                )
        );

        org.mockito.Mockito.when(
                candidateAvailability.evaluate(
                        tenantId,
                        snapshot,
                        p2,
                        "MONDAY",
                        teacherId,
                        null
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                "COMPLETE",
                result.candidateStatus()
        );

        assertEquals(
                1,
                result.requiredPeriods()
        );

        assertEquals(
                1,
                result.placedPeriods()
        );

        assertTrue(
                result.unplaced().isEmpty()
        );

        TimetableGenerationCandidate.Placement placement =
                result.placements()
                        .get(0);

        assertEquals(
                "MONDAY",
                placement.dayOfWeek()
        );

        assertEquals(
                p2.bellPeriodId(),
                placement.bellPeriodId()
        );

        assertEquals(
                "P2",
                placement.periodCode()
        );

        assertEquals(
                teacherId,
                placement.teacherProfileId()
        );

        org.mockito.Mockito.verify(
                candidateAvailability
        ).evaluate(
                tenantId,
                snapshot,
                p1,
                "MONDAY",
                teacherId,
                null
        );

        org.mockito.Mockito.verify(
                candidateAvailability
        ).evaluate(
                tenantId,
                snapshot,
                p2,
                "MONDAY",
                teacherId,
                null
        );
    }

    @Test
    void prefersSubjectSpecializedResourceOverGenericResource() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        UUID genericResourceId = UUID.randomUUID();
        UUID specializedResourceId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshotWithResources(
                        List.of("MONDAY"),
                        List.of(slot("P1", 1)),
                        List.of(
                                classDemandWithCapacity(
                                        classId,
                                        "P7",
                                        gradeId,
                                        35
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "SCI-P7",
                                        classId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-SCI-P7",
                                        teacherId,
                                        gradeId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                resource(
                                        genericResourceId,
                                        "GENERIC-ROOM",
                                        45,
                                        null
                                ),
                                resource(
                                        specializedResourceId,
                                        "SCIENCE-LAB",
                                        45,
                                        subjectId
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                "COMPLETE",
                result.candidateStatus()
        );

        assertEquals(
                specializedResourceId,
                result.placements()
                        .get(0)
                        .schedulingResourceId()
        );
    }

    @Test
    void neverSelectsResourceSpecializedForDifferentSubject() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        UUID wrongSubjectResourceId = UUID.randomUUID();
        UUID genericResourceId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshotWithResources(
                        List.of("MONDAY"),
                        List.of(slot("P1", 1)),
                        List.of(
                                classDemandWithCapacity(
                                        classId,
                                        "P6",
                                        gradeId,
                                        30
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "MATH-P6",
                                        classId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-MATH-P6",
                                        UUID.randomUUID(),
                                        gradeId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                resource(
                                        wrongSubjectResourceId,
                                        "ICT-LAB",
                                        40,
                                        UUID.randomUUID()
                                ),
                                resource(
                                        genericResourceId,
                                        "CLASSROOM-P6",
                                        40,
                                        null
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                "COMPLETE",
                result.candidateStatus()
        );

        assertEquals(
                genericResourceId,
                result.placements()
                        .get(0)
                        .schedulingResourceId()
        );

        assertNotEquals(
                wrongSubjectResourceId,
                result.placements()
                        .get(0)
                        .schedulingResourceId()
        );
    }

    @Test
    void rejectsUndersizedResourceAndSelectsAdequateResource() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        UUID smallRoomId = UUID.randomUUID();
        UUID adequateRoomId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshotWithResources(
                        List.of("MONDAY"),
                        List.of(slot("P1", 1)),
                        List.of(
                                classDemandWithCapacity(
                                        classId,
                                        "P5",
                                        gradeId,
                                        42
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "ENG-P5",
                                        classId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-ENG-P5",
                                        UUID.randomUUID(),
                                        gradeId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                resource(
                                        smallRoomId,
                                        "A-SMALL",
                                        30,
                                        null
                                ),
                                resource(
                                        adequateRoomId,
                                        "B-ADEQUATE",
                                        50,
                                        null
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                adequateRoomId,
                result.placements()
                        .get(0)
                        .schedulingResourceId()
        );
    }

    @Test
    void skipsUnavailableResourceAndSelectsNextAvailableResource() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        UUID unavailableRoomId = UUID.randomUUID();
        UUID availableRoomId = UUID.randomUUID();

        TimetableGenerationSnapshot.BellSlot p1 =
                slot("P1", 1);

        TimetableGenerationSnapshot snapshot =
                snapshotWithResources(
                        List.of("MONDAY"),
                        List.of(p1),
                        List.of(
                                classDemandWithCapacity(
                                        classId,
                                        "P4",
                                        gradeId,
                                        35
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "SST-P4",
                                        classId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-SST-P4",
                                        teacherId,
                                        gradeId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                resource(
                                        unavailableRoomId,
                                        "A-ROOM",
                                        40,
                                        null
                                ),
                                resource(
                                        availableRoomId,
                                        "B-ROOM",
                                        40,
                                        null
                                )
                        )
                );

        org.mockito.Mockito.when(
                candidateAvailability.evaluate(
                        tenantId,
                        snapshot,
                        p1,
                        "MONDAY",
                        null,
                        unavailableRoomId
                )
        ).thenReturn(
                java.util.Optional.of(
                        new TimetableAvailabilityConflict(
                                "RESOURCE_UNAVAILABLE",
                                "A-ROOM is under maintenance"
                        )
                )
        );

        org.mockito.Mockito.when(
                candidateAvailability.evaluate(
                        tenantId,
                        snapshot,
                        p1,
                        "MONDAY",
                        null,
                        availableRoomId
                )
        ).thenReturn(
                java.util.Optional.empty()
        );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                availableRoomId,
                result.placements()
                        .get(0)
                        .schedulingResourceId()
        );

        org.mockito.Mockito.verify(
                candidateAvailability
        ).evaluate(
                tenantId,
                snapshot,
                p1,
                "MONDAY",
                null,
                unavailableRoomId
        );

        org.mockito.Mockito.verify(
                candidateAvailability
        ).evaluate(
                tenantId,
                snapshot,
                p1,
                "MONDAY",
                null,
                availableRoomId
        );
    }

    @Test
    void doesNotDoubleBookSameResourceForSimultaneousLessons() {

        UUID grade1 = UUID.randomUUID();
        UUID grade2 = UUID.randomUUID();

        UUID class1 = UUID.randomUUID();
        UUID class2 = UUID.randomUUID();

        UUID subject1 = UUID.randomUUID();
        UUID subject2 = UUID.randomUUID();

        UUID resourceId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshotWithResources(
                        List.of("MONDAY"),
                        List.of(slot("P1", 1)),
                        List.of(
                                classDemandWithCapacity(
                                        class1,
                                        "P6",
                                        grade1,
                                        30
                                ),
                                classDemandWithCapacity(
                                        class2,
                                        "P7",
                                        grade2,
                                        30
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "A-SUBJECT",
                                        class1,
                                        null,
                                        subject1,
                                        1
                                ),
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "B-SUBJECT",
                                        class2,
                                        null,
                                        subject2,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-A",
                                        UUID.randomUUID(),
                                        grade1,
                                        null,
                                        subject1,
                                        1
                                ),
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-B",
                                        UUID.randomUUID(),
                                        grade2,
                                        null,
                                        subject2,
                                        1
                                )
                        ),
                        List.of(
                                resource(
                                        resourceId,
                                        "SHARED-ROOM",
                                        40,
                                        null
                                )
                        )
                );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                "COMPLETE",
                result.candidateStatus()
        );

        assertEquals(
                2,
                result.placedPeriods()
        );

        long assignedToSharedRoom =
                result.placements()
                        .stream()
                        .filter(
                                placement ->
                                        resourceId.equals(
                                                placement.schedulingResourceId()
                                        )
                        )
                        .count();

        long withoutResource =
                result.placements()
                        .stream()
                        .filter(
                                placement ->
                                        placement.schedulingResourceId()
                                                == null
                        )
                        .count();

        assertEquals(
                1,
                assignedToSharedRoom
        );

        assertEquals(
                1,
                withoutResource
        );
    }

    @Test
    void allowsLessonWithoutResourceWhenNoResourcesAreConfigured() {

        UUID gradeId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        TimetableGenerationSnapshot snapshot =
                snapshotWithResources(
                        List.of("MONDAY"),
                        List.of(slot("P1", 1)),
                        List.of(
                                classDemandWithCapacity(
                                        classId,
                                        "P3",
                                        gradeId,
                                        25
                                )
                        ),
                        List.of(
                                subjectDemand(
                                        UUID.randomUUID(),
                                        "LIT-P3",
                                        classId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of(
                                assignment(
                                        UUID.randomUUID(),
                                        "TA-LIT-P3",
                                        UUID.randomUUID(),
                                        gradeId,
                                        null,
                                        subjectId,
                                        1
                                )
                        ),
                        List.of()
                );

        TimetableGenerationCandidate result =
                generator.generate(
                        tenantId,
                        snapshot
                );

        assertEquals(
                "COMPLETE",
                result.candidateStatus()
        );

        assertEquals(
                1,
                result.placedPeriods()
        );

        assertNull(
                result.placements()
                        .get(0)
                        .schedulingResourceId()
        );
    }

    private TimetableGenerationSnapshot snapshotWithResources(
            List<String> days,
            List<TimetableGenerationSnapshot.BellSlot> slots,
            List<TimetableGenerationSnapshot.ClassDemand> classes,
            List<TimetableGenerationSnapshot.SubjectDemand> subjects,
            List<TimetableGenerationSnapshot.TeachingSupply> assignments,
            List<TimetableGenerationSnapshot.ResourceSupply> resources
    ) {

        TimetableGenerationSnapshot base =
                snapshot(
                        days,
                        slots,
                        classes,
                        subjects,
                        assignments
                );

        return new TimetableGenerationSnapshot(

                base.generationRequestId(),

                base.generationCode(),

                base.scope(),

                base.enabledDays(),

                base.bellSlots(),

                base.classes(),

                base.subjects(),

                base.streams(),

                base.teachingAssignments(),

                resources
        );
    }

    private TimetableGenerationSnapshot.ClassDemand classDemandWithCapacity(
            UUID classOfferingId,
            String code,
            UUID gradeId,
            Integer plannedCapacity
    ) {

        return new TimetableGenerationSnapshot.ClassDemand(

                classOfferingId,

                code,

                gradeId,

                plannedCapacity
        );
    }

    private TimetableGenerationSnapshot.ResourceSupply resource(
            UUID resourceId,
            String code,
            Integer capacity,
            UUID specializedForSubjectId
    ) {

        return new TimetableGenerationSnapshot.ResourceSupply(

                resourceId,

                code,

                code,

                "CLASSROOM",

                capacity,

                specializedForSubjectId,

                true,

                "ACTIVE"
        );
    }

    private TimetableGenerationSnapshot snapshot(
            List<String> days,
            List<TimetableGenerationSnapshot.BellSlot> slots,
            List<TimetableGenerationSnapshot.ClassDemand> classes,
            List<TimetableGenerationSnapshot.SubjectDemand> subjects,
            List<TimetableGenerationSnapshot.TeachingSupply> assignments
    ) {

        UUID academicYearId =
                UUID.randomUUID();

        UUID academicTermId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID bellScheduleId =
                UUID.randomUUID();

        return new TimetableGenerationSnapshot(

                UUID.randomUUID(),

                "GEN-TEST",

                new TimetableGenerationSnapshot.Scope(

                        academicYearId,

                        academicTermId,

                        campusId,

                        bellScheduleId,

                        "MASTER",

                        LocalDate.of(
                                2026,
                                2,
                                1
                        ),

                        LocalDate.of(
                                2026,
                                4,
                                30
                        ),

                        "Africa/Kampala",

                        "RULE_ENGINE",

                        null,

                        "Behavior verification"
                ),

                days,

                slots,

                classes,

                subjects,

                List.of(),

                assignments,

                List.of()
        );
    }

    private TimetableGenerationSnapshot.BellSlot slot(
            String code,
            int sequence
    ) {

        LocalTime start =
                LocalTime.of(
                        8,
                        0
                ).plusMinutes(
                        40L * (sequence - 1)
                );

        return new TimetableGenerationSnapshot.BellSlot(

                UUID.randomUUID(),

                code,

                sequence,

                "TEACHING",

                start,

                start.plusMinutes(40)
        );
    }

    private TimetableGenerationSnapshot.ClassDemand classDemand(
            UUID classOfferingId,
            String code,
            UUID gradeId
    ) {

        return new TimetableGenerationSnapshot.ClassDemand(

                classOfferingId,

                code,

                gradeId,

                null
        );
    }

    private TimetableGenerationSnapshot.SubjectDemand subjectDemand(
            UUID subjectOfferingId,
            String code,
            UUID classOfferingId,
            UUID streamId,
            UUID subjectId,
            int weeklyPeriods
    ) {

        return new TimetableGenerationSnapshot.SubjectDemand(

                subjectOfferingId,

                code,

                classOfferingId,

                UUID.randomUUID(),

                streamId,

                subjectId,

                weeklyPeriods
        );
    }

    private TimetableGenerationSnapshot.TeachingSupply assignment(
            UUID assignmentId,
            String reference,
            UUID teacherId,
            UUID gradeId,
            UUID streamId,
            UUID subjectId,
            int weeklyPeriods
    ) {

        return new TimetableGenerationSnapshot.TeachingSupply(

                assignmentId,

                reference,

                teacherId,

                UUID.randomUUID(),

                UUID.randomUUID(),

                UUID.randomUUID(),

                gradeId,

                streamId,

                subjectId,

                weeklyPeriods,

                new BigDecimal("100.00"),

                LocalDate.of(
                        2026,
                        2,
                        1
                ),

                LocalDate.of(
                        2026,
                        4,
                        30
                )
        );
    }
}
