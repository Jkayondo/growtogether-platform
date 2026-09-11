package africa.growtogether.platform.school.timetable.entry;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimetableEntryLifecycleTest {

    @Test
    void followsControlledEntryLifecycle() {

        TimetableEntry entry = entry();

        assertEquals(
                "SCHEDULED",
                entry.getEntryStatus()
        );

        entry.confirm();

        assertEquals(
                "CONFIRMED",
                entry.getEntryStatus()
        );

        entry.activate();

        assertEquals(
                "ACTIVE",
                entry.getEntryStatus()
        );

        entry.complete();

        assertEquals(
                "COMPLETED",
                entry.getEntryStatus()
        );
    }

    @Test
    void rejectsCompletionBeforeEntryIsActive() {

        TimetableEntry entry = entry();

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        entry::complete
                );

        assertEquals(
                "Only ACTIVE entries can be completed",
                error.getMessage()
        );

        assertEquals(
                "SCHEDULED",
                entry.getEntryStatus()
        );
    }

    @Test
    void preventsTerminalEntryFromBeingRewritten() {

        TimetableEntry entry = entry();

        entry.cancel();

        assertEquals(
                "CANCELLED",
                entry.getEntryStatus()
        );

        assertThrows(
                IllegalStateException.class,
                entry::complete
        );

        assertThrows(
                IllegalStateException.class,
                entry::markMoved
        );

        assertThrows(
                IllegalStateException.class,
                entry::markReplaced
        );

        assertEquals(
                "CANCELLED",
                entry.getEntryStatus()
        );
    }

    @Test
    void allowsOpenEntryToBeMovedOrReplaced() {

        TimetableEntry moved = entry();
        moved.markMoved();

        assertEquals(
                "MOVED",
                moved.getEntryStatus()
        );

        TimetableEntry replaced = entry();
        replaced.confirm();
        replaced.markReplaced();

        assertEquals(
                "REPLACED",
                replaced.getEntryStatus()
        );
    }

    private TimetableEntry entry() {

        return new TimetableEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "MONDAY",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "LESSON",
                null,
                null,
                true,
                null,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30)
        );
    }
}
