package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.school.timetable.core.CreateTimetableCommand;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.core.TimetableRepository;
import africa.growtogether.platform.school.timetable.core.TimetableService;

import africa.growtogether.platform.school.timetable.entry.CreateTimetableEntryCommand;
import africa.growtogether.platform.school.timetable.entry.TimetableEntry;
import africa.growtogether.platform.school.timetable.entry.TimetableEntryService;

import africa.growtogether.platform.school.timetable.reliability.TimetableChangeHistoryService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class TimetableGenerationExecutionService {

    private final TimetableGenerationRequestRepository requests;

    private final TimetableGenerationSnapshotService snapshots;

    private final DeterministicTimetableCandidateGenerator generator;

    private final TimetableService timetables;

    private final TimetableRepository timetableRepository;

    private final TimetableEntryService entries;

    private final TimetableChangeHistoryService history;


    public TimetableGenerationExecutionService(
            TimetableGenerationRequestRepository requests,
            TimetableGenerationSnapshotService snapshots,
            DeterministicTimetableCandidateGenerator generator,
            TimetableService timetables,
            TimetableRepository timetableRepository,
            TimetableEntryService entries,
            TimetableChangeHistoryService history
    ) {

        this.requests = requests;
        this.snapshots = snapshots;
        this.generator = generator;
        this.timetables = timetables;
        this.timetableRepository = timetableRepository;
        this.entries = entries;
        this.history = history;
    }


    /*
     * GT-AI-PRINCIPLE-001
     *
     * Generation intelligence is not authoritative persistence.
     *
     * A candidate must first be complete. It is then persisted only
     * through the existing timetable and timetable-entry services so
     * all authoritative tenant, academic, teacher, class, resource,
     * availability and conflict rules remain in force.
     *
     * This transaction deliberately stops at GENERATED.
     * Human review, approval, publication and activation remain
     * separate governed operations.
     */
    @Transactional
    public Timetable execute(
            UUID tenantId,
            UUID requestId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "requestId must not be null"
            );
        }

        TimetableGenerationRequest request =
                requests
                        .findByTenantIdAndId(
                                tenantId,
                                requestId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Timetable generation request not found"
                                )
                        );

        if (
                !"READY".equals(
                        request.getGenerationStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Timetable generation request must be READY but is "
                            + request.getGenerationStatus()
            );
        }

        /*
         * Build and validate the candidate while the request remains
         * READY. An incomplete candidate therefore cannot leave the
         * request stranded in GENERATING.
         */
        TimetableGenerationSnapshot snapshot =
                snapshots.build(
                        tenantId,
                        requestId
                );

        TimetableGenerationCandidate candidate =
                generator.generate(
                        tenantId,
                        snapshot
                );

        if (
                !"COMPLETE".equals(
                        candidate.candidateStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Timetable candidate is incomplete: placed "
                            + candidate.placedPeriods()
                            + " of "
                            + candidate.requiredPeriods()
                            + " required periods"
            );
        }

        /*
         * Only now do we enter the generation lifecycle.
         *
         * Any subsequent authoritative persistence failure rolls this
         * entire transaction back, including this status transition.
         */
        request.beginGeneration();

        requests.save(
                request
        );

        int versionNumber =
                nextVersion(
                        tenantId,
                        snapshot.scope()
                );

        Timetable timetable =
                timetables.create(
                        tenantId,
                        new CreateTimetableCommand(
                                generatedTimetableCode(
                                        requestId
                                ),
                                "Generated Timetable "
                                        + snapshot.generationCode(),
                                "Generated from governed timetable request "
                                        + snapshot.generationCode(),
                                snapshot.scope()
                                        .academicYearId(),
                                snapshot.scope()
                                        .academicTermId(),
                                snapshot.scope()
                                        .campusId(),
                                snapshot.scope()
                                        .bellScheduleId(),
                                snapshot.scope()
                                        .timetableType(),
                                versionNumber,
                                snapshot.scope()
                                        .effectiveFrom(),
                                snapshot.scope()
                                        .effectiveTo(),
                                snapshot.scope()
                                        .generationMode(),
                                requestId,
                                null
                        )
                );

        for (
                TimetableGenerationCandidate.Placement placement
                : candidate.placements()
        ) {

            TimetableEntry entry =
                    entries.create(
                            tenantId,
                            new CreateTimetableEntryCommand(
                                    timetable.getId(),
                                    placement.bellPeriodId(),
                                    placement.dayOfWeek(),
                                    placement.classOfferingId(),
                                    placement.subjectOfferingId(),
                                    placement.classGradeId(),
                                    placement.streamId(),
                                    placement.teachingAssignmentId(),
                                    placement.teacherProfileId(),
                                    placement.schedulingResourceId(),
                                    "LESSON",
                                    null,
                                    placement.placementReason(),
                                    Boolean.TRUE,
                                    null,
                                    snapshot.scope()
                                            .effectiveFrom(),
                                    snapshot.scope()
                                            .effectiveTo()
                            )
                    );

            history.record(
                    tenantId,
                    timetable.getId(),
                    entry.getId(),
                    "ENTRY_ADDED",
                    placement.placementReason(),
                    null,
                    null,
                    requestId.toString(),
                    false,
                    "gt-timetable-generation"
            );
        }

        history.record(
                tenantId,
                timetable.getId(),
                null,
                "TIMETABLE_GENERATED",
                "Generated "
                        + candidate.placedPeriods()
                        + " of "
                        + candidate.requiredPeriods()
                        + " required periods from generation request "
                        + snapshot.generationCode(),
                null,
                null,
                requestId.toString(),
                false,
                "gt-timetable-generation"
        );

        request.markGenerated(
                timetable.getId()
        );

        requests.save(
                request
        );

        return timetable;
    }


    /*
     * IMPROVEMENT:
     *
     * Determine the next version from existing authoritative
     * timetables instead of assuming version 1.
     */
    private int nextVersion(
            UUID tenantId,
            TimetableGenerationSnapshot.Scope scope
    ) {

        return timetableRepository
                .findByTenantIdAndAcademicYearId(
                        tenantId,
                        scope.academicYearId()
                )
                .stream()
                .filter(
                        timetable ->
                                Objects.equals(
                                        timetable.getAcademicTermId(),
                                        scope.academicTermId()
                                )
                )
                .filter(
                        timetable ->
                                Objects.equals(
                                        timetable.getCampusId(),
                                        scope.campusId()
                                )
                )
                .filter(
                        timetable ->
                                timetable.getTimetableType() != null
                                && timetable
                                        .getTimetableType()
                                        .equalsIgnoreCase(
                                                scope.timetableType()
                                        )
                )
                .map(
                        Timetable::getVersionNumber
                )
                .filter(
                        Objects::nonNull
                )
                .mapToInt(
                        Integer::intValue
                )
                .max()
                .orElse(0)
                + 1;
    }


    private String generatedTimetableCode(
            UUID requestId
    ) {

        /*
         * Request UUID gives us a deterministic tenant-independent
         * unique generation-derived timetable code without relying on
         * human naming conventions.
         */
        return "TT-GEN-"
                + requestId;
    }
}
