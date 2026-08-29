package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.school.timetable.core.CreateTimetableCommand;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;
import africa.growtogether.platform.school.timetable.core.TimetableService;

import africa.growtogether.platform.school.timetable.entry.CreateTimetableEntryCommand;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import africa.growtogether.platform.school.timetable.entry.TimetableEntryService;

import africa.growtogether.platform.school.timetable.reliability.TimetableChangeHistoryService;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimetableGenerationExecutionServiceTest {

    private final TimetableGenerationRequestRepository requests =
            mock(TimetableGenerationRequestRepository.class);

    private final TimetableGenerationSnapshotService snapshots =
            mock(TimetableGenerationSnapshotService.class);

    private final DeterministicTimetableCandidateGenerator generator =
            mock(DeterministicTimetableCandidateGenerator.class);

    private final TimetableService timetables =
            mock(TimetableService.class);

    private final TimetableRepository timetableRepository =
            mock(TimetableRepository.class);

    private final TimetableEntryService entries =
            mock(TimetableEntryService.class);

    private final TimetableChangeHistoryService history =
            mock(TimetableChangeHistoryService.class);

    private final TimetableGenerationExecutionService service =
            new TimetableGenerationExecutionService(
                    requests,
                    snapshots,
                    generator,
                    timetables,
                    timetableRepository,
                    entries,
                    history
            );


    @Test
    void completeCandidateCreatesAuthoritativeTimetableAndMarksRequestGenerated() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID timetableId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();

        TimetableGenerationRequest request =
                readyRequest(
                        tenantId,
                        requestId
                );

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        requestId,
                        "AI_ASSISTED"
                );

        TimetableGenerationCandidate.Placement placement =
                placement();

        TimetableGenerationCandidate candidate =
                new TimetableGenerationCandidate(
                        requestId,
                        "COMPLETE",
                        1,
                        1,
                        List.of(
                                placement
                        ),
                        List.of()
                );

        when(
                snapshots.build(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                snapshot
        );

        when(
                generator.generate(
                        tenantId,
                        snapshot
                )
        ).thenReturn(
                candidate
        );

        when(
                timetableRepository
                        .findByTenantIdAndAcademicYearId(
                                tenantId,
                                snapshot.scope()
                                        .academicYearId()
                        )
        ).thenReturn(
                List.of()
        );

        Timetable timetable =
                mock(Timetable.class);

        when(
                timetable.getId()
        ).thenReturn(
                timetableId
        );

        when(
                timetables.create(
                        eq(tenantId),
                        any(CreateTimetableCommand.class)
                )
        ).thenReturn(
                timetable
        );

        TimetableEntry entry =
                mock(TimetableEntry.class);

        when(
                entry.getId()
        ).thenReturn(
                entryId
        );

        when(
                entries.create(
                        eq(tenantId),
                        any(CreateTimetableEntryCommand.class)
                )
        ).thenReturn(
                entry
        );

        Timetable result =
                service.execute(
                        tenantId,
                        requestId
                );

        assertSame(
                timetable,
                result
        );

        verify(
                request
        ).beginGeneration();

        verify(
                request
        ).markGenerated(
                timetableId
        );

        ArgumentCaptor<CreateTimetableCommand>
                timetableCommand =
                ArgumentCaptor.forClass(
                        CreateTimetableCommand.class
                );

        verify(
                timetables
        ).create(
                eq(tenantId),
                timetableCommand.capture()
        );

        assertEquals(
                1,
                timetableCommand
                        .getValue()
                        .versionNumber()
        );

        assertEquals(
                "AI_ASSISTED",
                timetableCommand
                        .getValue()
                        .generatedBy()
        );

        assertEquals(
                requestId,
                timetableCommand
                        .getValue()
                        .generationReference()
        );

        ArgumentCaptor<CreateTimetableEntryCommand>
                entryCommand =
                ArgumentCaptor.forClass(
                        CreateTimetableEntryCommand.class
                );

        verify(
                entries
        ).create(
                eq(tenantId),
                entryCommand.capture()
        );

        assertEquals(
                timetableId,
                entryCommand
                        .getValue()
                        .timetableId()
        );

        assertEquals(
                placement.bellPeriodId(),
                entryCommand
                        .getValue()
                        .bellPeriodId()
        );

        assertEquals(
                placement.teacherProfileId(),
                entryCommand
                        .getValue()
                        .teacherProfileId()
        );

        assertEquals(
                placement.schedulingResourceId(),
                entryCommand
                        .getValue()
                        .schedulingResourceId()
        );

        assertEquals(
                "LESSON",
                entryCommand
                        .getValue()
                        .entryType()
        );

        assertTrue(
                Boolean.TRUE.equals(
                        entryCommand
                                .getValue()
                                .recurring()
                )
        );

        verify(
                history
        ).record(
                eq(tenantId),
                eq(timetableId),
                eq(entryId),
                eq("ENTRY_ADDED"),
                eq(placement.placementReason()),
                isNull(),
                isNull(),
                eq(requestId.toString()),
                eq(false),
                eq("gt-timetable-generation")
        );

        verify(
                history
        ).record(
                eq(tenantId),
                eq(timetableId),
                isNull(),
                eq("TIMETABLE_GENERATED"),
                contains("Generated 1 of 1"),
                isNull(),
                isNull(),
                eq(requestId.toString()),
                eq(false),
                eq("gt-timetable-generation")
        );

        /*
         * Human-governed lifecycle must remain separate.
         */
        verify(
                timetables,
                never()
        ).submitForReview(
                any(),
                any()
        ,
                any());

        verify(
                timetables,
                never()
        ).approve(
                any(),
                any(),
                any()
        );

        verify(
                timetables,
                never()
        ).publish(
                any(),
                any(),
                any()
        );

        verify(
                timetables,
                never()
        ).activate(
                any(),
                any()
        ,
                any());
    }


    @Test
    void incompleteCandidateDoesNotEnterGenerationOrPersistTimetable() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        TimetableGenerationRequest request =
                readyRequest(
                        tenantId,
                        requestId
                );

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        requestId,
                        "RULE_ENGINE"
                );

        TimetableGenerationCandidate candidate =
                new TimetableGenerationCandidate(
                        requestId,
                        "INCOMPLETE",
                        5,
                        4,
                        List.of(),
                        List.of()
                );

        when(
                snapshots.build(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                snapshot
        );

        when(
                generator.generate(
                        tenantId,
                        snapshot
                )
        ).thenReturn(
                candidate
        );

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.execute(
                                        tenantId,
                                        requestId
                                )
                );

        assertTrue(
                error.getMessage()
                        .contains(
                                "candidate is incomplete"
                        )
        );

        verify(
                request,
                never()
        ).beginGeneration();

        verify(
                request,
                never()
        ).markGenerated(
                any()
        );

        verify(
                timetables,
                never()
        ).create(
                any(),
                any()
        );

        verify(
                entries,
                never()
        ).create(
                any(),
                any()
        );

        verifyNoInteractions(
                history
        );
    }


    @Test
    void rejectsRequestThatIsNotReadyBeforeBuildingSnapshot() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        TimetableGenerationRequest request =
                mock(TimetableGenerationRequest.class);

        when(
                request.getGenerationStatus()
        ).thenReturn(
                "GENERATED"
        );

        when(
                requests.findByTenantIdAndId(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.of(
                        request
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.execute(
                                tenantId,
                                requestId
                        )
        );

        verifyNoInteractions(
                snapshots
        );

        verifyNoInteractions(
                generator
        );

        verifyNoInteractions(
                timetables
        );

        verifyNoInteractions(
                entries
        );
    }


    @Test
    void calculatesNextVersionWithinSameAcademicScope() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        TimetableGenerationRequest request =
                readyRequest(
                        tenantId,
                        requestId
                );

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        requestId,
                        "RULE_ENGINE"
                );

        TimetableGenerationCandidate candidate =
                new TimetableGenerationCandidate(
                        requestId,
                        "COMPLETE",
                        0,
                        0,
                        List.of(),
                        List.of()
                );

        when(
                snapshots.build(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                snapshot
        );

        when(
                generator.generate(
                        tenantId,
                        snapshot
                )
        ).thenReturn(
                candidate
        );

        Timetable matchingV3 =
                mock(Timetable.class);

        when(
                matchingV3.getAcademicTermId()
        ).thenReturn(
                snapshot.scope()
                        .academicTermId()
        );

        when(
                matchingV3.getCampusId()
        ).thenReturn(
                snapshot.scope()
                        .campusId()
        );

        when(
                matchingV3.getTimetableType()
        ).thenReturn(
                snapshot.scope()
                        .timetableType()
        );

        when(
                matchingV3.getVersionNumber()
        ).thenReturn(
                3
        );

        Timetable differentCampusV9 =
                mock(Timetable.class);

        when(
                differentCampusV9.getAcademicTermId()
        ).thenReturn(
                snapshot.scope()
                        .academicTermId()
        );

        when(
                differentCampusV9.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                differentCampusV9.getTimetableType()
        ).thenReturn(
                snapshot.scope()
                        .timetableType()
        );

        when(
                differentCampusV9.getVersionNumber()
        ).thenReturn(
                9
        );

        when(
                timetableRepository
                        .findByTenantIdAndAcademicYearId(
                                tenantId,
                                snapshot.scope()
                                        .academicYearId()
                        )
        ).thenReturn(
                List.of(
                        matchingV3,
                        differentCampusV9
                )
        );

        Timetable generated =
                mock(Timetable.class);

        UUID generatedId =
                UUID.randomUUID();

        when(
                generated.getId()
        ).thenReturn(
                generatedId
        );

        when(
                timetables.create(
                        eq(tenantId),
                        any(CreateTimetableCommand.class)
                )
        ).thenReturn(
                generated
        );

        service.execute(
                tenantId,
                requestId
        );

        ArgumentCaptor<CreateTimetableCommand>
                command =
                ArgumentCaptor.forClass(
                        CreateTimetableCommand.class
                );

        verify(
                timetables
        ).create(
                eq(tenantId),
                command.capture()
        );

        assertEquals(
                4,
                command
                        .getValue()
                        .versionNumber()
        );

        verify(
                request
        ).markGenerated(
                generatedId
        );
    }


    @Test
    void authoritativeEntryFailureDoesNotMarkRequestGenerated() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        TimetableGenerationRequest request =
                readyRequest(
                        tenantId,
                        requestId
                );

        TimetableGenerationSnapshot snapshot =
                snapshot(
                        requestId,
                        "RULE_ENGINE"
                );

        TimetableGenerationCandidate candidate =
                new TimetableGenerationCandidate(
                        requestId,
                        "COMPLETE",
                        1,
                        1,
                        List.of(
                                placement()
                        ),
                        List.of()
                );

        when(
                snapshots.build(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                snapshot
        );

        when(
                generator.generate(
                        tenantId,
                        snapshot
                )
        ).thenReturn(
                candidate
        );

        when(
                timetableRepository
                        .findByTenantIdAndAcademicYearId(
                                tenantId,
                                snapshot.scope()
                                        .academicYearId()
                        )
        ).thenReturn(
                List.of()
        );

        Timetable timetable =
                mock(Timetable.class);

        when(
                timetable.getId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                timetables.create(
                        eq(tenantId),
                        any(CreateTimetableCommand.class)
                )
        ).thenReturn(
                timetable
        );

        when(
                entries.create(
                        eq(tenantId),
                        any(CreateTimetableEntryCommand.class)
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Authoritative timetable entry rejected"
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.execute(
                                        tenantId,
                                        requestId
                                )
                );

        assertEquals(
                "Authoritative timetable entry rejected",
                error.getMessage()
        );

        verify(
                request
        ).beginGeneration();

        verify(
                request,
                never()
        ).markGenerated(
                any()
        );

        verifyNoInteractions(
                history
        );
    }


    private TimetableGenerationRequest readyRequest(
            UUID tenantId,
            UUID requestId
    ) {

        TimetableGenerationRequest request =
                mock(TimetableGenerationRequest.class);

        when(
                request.getGenerationStatus()
        ).thenReturn(
                "READY"
        );

        when(
                requests.findByTenantIdAndId(
                        tenantId,
                        requestId
                )
        ).thenReturn(
                Optional.of(
                        request
                )
        );

        return request;
    }


    private TimetableGenerationSnapshot snapshot(
            UUID requestId,
            String generationMode
    ) {

        return new TimetableGenerationSnapshot(

                requestId,

                "GEN-R1-001",

                new TimetableGenerationSnapshot.Scope(

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

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

                        generationMode,

                        null,

                        "Behavior verification"
                ),

                List.of(
                        "MONDAY"
                ),

                List.of(),

                List.of(),

                List.of(),

                List.of(),

                List.of(),

                List.of()
        );
    }


    private TimetableGenerationCandidate.Placement placement() {

        return new TimetableGenerationCandidate.Placement(

                "MONDAY",

                UUID.randomUUID(),

                "P1",

                UUID.randomUUID(),

                UUID.randomUUID(),

                UUID.randomUUID(),

                null,

                UUID.randomUUID(),

                UUID.randomUUID(),

                UUID.randomUUID(),

                UUID.randomUUID(),

                "Governed deterministic placement"
        );
    }
}
