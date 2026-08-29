package africa.growtogether.platform.school.timetable.reliability;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimetableConflictServiceTest {

    @Test
    void recordsDetectedConflictAndMatchingChangeHistory() {

        TimetableConflictRepository repository =
                mock(TimetableConflictRepository.class);

        TimetableChangeHistoryService history =
                mock(TimetableChangeHistoryService.class);

        TimetableConflictService service =
                new TimetableConflictService(
                        repository,
                        history
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        UUID conflictingEntryId =
                UUID.randomUUID();

        when(
                repository.save(
                        any(TimetableConflict.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        TimetableConflict result =
                service.recordDetectedConflict(
                        tenantId,
                        timetableId,
                        null,
                        conflictingEntryId,
                        "teacher_double_booking",
                        "error",
                        "Teacher T-001 is already scheduled",
                        "system"
                );

        assertNotNull(result);

        assertEquals(
                timetableId,
                result.getTimetableId()
        );

        assertEquals(
                conflictingEntryId,
                result.getConflictingEntryId()
        );

        /*
         * Controlled values are normalized.
         */
        assertEquals(
                "TEACHER_DOUBLE_BOOKING",
                result.getConflictType()
        );

        assertEquals(
                "ERROR",
                result.getConflictSeverity()
        );

        assertEquals(
                "SYSTEM",
                result.getDetectedBy()
        );

        assertFalse(
                result.isResolved()
        );

        assertNotNull(
                result.getDetectedAt()
        );

        verify(
                repository
        ).save(
                any(TimetableConflict.class)
        );

        verify(
                history
        ).record(
                eq(tenantId),
                eq(timetableId),
                isNull(),
                eq("CONFLICT_DETECTED"),
                eq("Teacher T-001 is already scheduled"),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq("system")
        );
    }

    @Test
    void resolvesConflictAndRecordsResolutionHistory() {

        TimetableConflictRepository repository =
                mock(TimetableConflictRepository.class);

        TimetableChangeHistoryService history =
                mock(TimetableChangeHistoryService.class);

        TimetableConflictService service =
                new TimetableConflictService(
                        repository,
                        history
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID timetableId =
                UUID.randomUUID();

        UUID conflictId =
                UUID.randomUUID();

        UUID resolvedBy =
                UUID.randomUUID();

        TimetableConflict conflict =
                new TimetableConflict(
                        timetableId,
                        null,
                        UUID.randomUUID(),
                        "CLASS_DOUBLE_BOOKING",
                        "ERROR",
                        "Senior 2 is already scheduled",
                        "SYSTEM"
                );

        conflict.setTenantId(
                tenantId
        );

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        conflictId
                )
        ).thenReturn(
                Optional.of(conflict)
        );

        when(
                repository.save(
                        conflict
                )
        ).thenReturn(
                conflict
        );

        TimetableConflict result =
                service.resolve(
                        tenantId,
                        conflictId,
                        resolvedBy,
                        "Moved the second lesson to Period 4"
                );

        assertTrue(
                result.isResolved()
        );

        assertEquals(
                resolvedBy,
                result.getResolvedBy()
        );

        assertNotNull(
                result.getResolvedAt()
        );

        assertEquals(
                "Moved the second lesson to Period 4",
                result.getResolutionNotes()
        );

        verify(
                repository
        ).save(
                conflict
        );

        verify(
                history
        ).record(
                eq(tenantId),
                eq(timetableId),
                isNull(),
                eq("CONFLICT_RESOLVED"),
                eq("Moved the second lesson to Period 4"),
                eq(resolvedBy),
                isNull(),
                isNull(),
                eq(false),
                eq(resolvedBy.toString())
        );
    }
}
