package africa.growtogether.platform.school.timetable.occurrence;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimetableOccurrenceResolverTest {

    private final TimetableOccurrenceResolver resolver =
            new TimetableOccurrenceResolver();

    @Test
    void resolvesNonRecurringEntryOnSingleDate() {

        LocalDate date =
                LocalDate.of(
                        2026,
                        8,
                        24
                );

        List<LocalDate> result =
                resolver.resolveEntry(
                        "MONDAY",
                        false,
                        null,
                        date,
                        date,
                        null,
                        null
                );

        assertEquals(
                List.of(date),
                result
        );
    }

    @Test
    void rejectsNonRecurringDateThatDoesNotMatchDayOfWeek() {

        LocalDate date =
                LocalDate.of(
                        2026,
                        8,
                        24
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> resolver.resolveEntry(
                                "TUESDAY",
                                false,
                                null,
                                date,
                                date,
                                null,
                                null
                        )
                );

        assertEquals(
                "Non-recurring timetable entry date does not match dayOfWeek",
                error.getMessage()
        );
    }

    @Test
    void resolvesEveryWeeklyOccurrenceAcrossEffectiveRange() {

        List<LocalDate> result =
                resolver.resolveEntry(
                        "MONDAY",
                        true,
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        ),
                        null,
                        null
                );

        assertEquals(
                List.of(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                31
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                ),
                result
        );
    }

    @Test
    void fallsBackToTimetableEffectiveDates() {

        List<LocalDate> result =
                resolver.resolveEntry(
                        "MONDAY",
                        true,
                        null,
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        )
                );

        assertEquals(
                3,
                result.size()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        8,
                        24
                ),
                result.get(0)
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        9,
                        7
                ),
                result.get(2)
        );
    }

    @Test
    void rejectsCustomRecurrenceRuleRatherThanIgnoringIt() {

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> resolver.resolveEntry(
                                "MONDAY",
                                true,
                                "FREQ=WEEKLY;BYDAY=MO",
                                LocalDate.of(
                                        2026,
                                        8,
                                        24
                                ),
                                LocalDate.of(
                                        2026,
                                        9,
                                        7
                                ),
                                null,
                                null
                        )
                );

        assertEquals(
                "Custom timetable recurrence rules are not yet supported for availability evaluation",
                error.getMessage()
        );
    }

    @Test
    void determinesWhetherEntryOccursOnRequestedDate() {

        assertTrue(
                resolver.occursOn(
                        "MONDAY",
                        true,
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        ),
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                31
                        )
                )
        );

        assertFalse(
                resolver.occursOn(
                        "MONDAY",
                        true,
                        null,
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                7
                        ),
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                9,
                                1
                        )
                )
        );
    }

    @Test
    void resolvesWeeklyCandidateRange() {

        assertEquals(
                List.of(
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                8,
                                31
                        )
                ),
                resolver.resolveWeeklyCandidates(
                        "MONDAY",
                        LocalDate.of(
                                2026,
                                8,
                                24
                        ),
                        LocalDate.of(
                                2026,
                                9,
                                6
                        )
                )
        );
    }
}
