package africa.growtogether.platform.school.timetable.reliability;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TimetableConflictService {

    private final TimetableConflictRepository repository;
    private final TimetableChangeHistoryService history;

    public TimetableConflictService(
            TimetableConflictRepository repository,
            TimetableChangeHistoryService history
    ) {
        this.repository = repository;
        this.history = history;
    }

    /*
     * IMPROVEMENT:
     * Conflict evidence and its CONFLICT_DETECTED history entry are
     * committed together in an independent transaction.
     *
     * Therefore, if the attempted timetable-entry transaction is rejected,
     * the governed evidence of the conflict remains available.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public TimetableConflict recordDetectedConflict(
            UUID tenantId,
            UUID timetableId,
            UUID timetableEntryId,
            UUID conflictingEntryId,
            String conflictType,
            String conflictSeverity,
            String description,
            String detectedBy
    ) {

        TimetableConflict conflict =
                new TimetableConflict(
                        timetableId,
                        timetableEntryId,
                        conflictingEntryId,
                        conflictType,
                        conflictSeverity,
                        description,
                        detectedBy
                );

        conflict.setTenantId(
                tenantId
        );

        TimetableConflict saved =
                repository.save(
                        conflict
                );

        history.record(
                tenantId,
                timetableId,
                timetableEntryId,
                "CONFLICT_DETECTED",
                description,
                null,
                null,
                saved.getId() == null
                        ? null
                        : saved.getId().toString(),
                false,
                "system"
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TimetableConflict> findUnresolved(
            UUID tenantId,
            UUID timetableId
    ) {

        return repository
                .findByTenantIdAndTimetableIdAndResolvedFalse(
                        tenantId,
                        timetableId
                );
    }

    @Transactional
    public TimetableConflict resolve(
            UUID tenantId,
            UUID conflictId,
            UUID resolvedBy,
            String resolutionNotes
    ) {

        TimetableConflict conflict =
                repository
                        .findByTenantIdAndId(
                                tenantId,
                                conflictId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Timetable conflict not found"
                                )
                        );

        conflict.resolve(
                resolvedBy,
                resolutionNotes
        );

        TimetableConflict saved =
                repository.save(
                        conflict
                );

        history.record(
                tenantId,
                saved.getTimetableId(),
                saved.getTimetableEntryId(),
                "CONFLICT_RESOLVED",
                resolutionNotes,
                resolvedBy,
                null,
                saved.getId() == null
                        ? null
                        : saved.getId().toString(),
                false,
                resolvedBy.toString()
        );

        return saved;
    }
}
