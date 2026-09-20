package africa.growtogether.platform.school.timetable.occurrence;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Governed timetable occurrence semantics shared by timetable
 * availability and consuming timetable views such as Teacher Programme.
 *
 * Supported:
 * - one-off/non-recurring entries;
 * - ordinary weekly recurrence by dayOfWeek;
 * - entry effective-date override with timetable-date fallback.
 *
 * Deliberately unsupported:
 * - custom recurrence-rule interpretation.
 *
 * Custom recurrence rules must fail closed until a governed recurrence
 * engine is introduced rather than being silently ignored.
 */
@Component
public class TimetableOccurrenceResolver {

    public List<LocalDate> resolveWeeklyCandidates(
            String dayOfWeek,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (effectiveTo == null) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be null"
            );
        }

        if (effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        return resolveWeeklyRange(
                requireDay(dayOfWeek),
                effectiveFrom,
                effectiveTo
        );
    }

    public List<LocalDate> resolveEntry(
            String dayOfWeek,
            Boolean recurring,
            String recurrenceRule,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            LocalDate timetableEffectiveFrom,
            LocalDate timetableEffectiveTo
    ) {

        DayOfWeek requiredDay =
                requireDay(
                        dayOfWeek
                );

        boolean repeats =
                recurring == null
                        || recurring;

        /*
         * Preserve the existing governed timetable contract:
         * weekly recurrence is supported; arbitrary recurrence
         * rules are not interpreted yet.
         */
        if (
                repeats
                && recurrenceRule != null
                && !recurrenceRule.isBlank()
        ) {
            throw new IllegalStateException(
                    "Custom timetable recurrence rules are not yet supported for availability evaluation"
            );
        }

        LocalDate start =
                effectiveFrom == null
                        ? timetableEffectiveFrom
                        : effectiveFrom;

        if (start == null) {
            throw new IllegalArgumentException(
                    "Timetable entry effectiveFrom is required for availability evaluation"
            );
        }

        if (!repeats) {

            if (
                    start.getDayOfWeek()
                            != requiredDay
            ) {
                throw new IllegalArgumentException(
                        "Non-recurring timetable entry date does not match dayOfWeek"
                );
            }

            return List.of(
                    start
            );
        }

        LocalDate end =
                effectiveTo == null
                        ? timetableEffectiveTo
                        : effectiveTo;

        if (end == null) {
            throw new IllegalArgumentException(
                    "Recurring timetable entry effectiveTo is required for availability evaluation"
            );
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Timetable entry effectiveTo must not be before effectiveFrom"
            );
        }

        return resolveWeeklyRange(
                requiredDay,
                start,
                end
        );
    }

    public boolean occursOn(
            String dayOfWeek,
            Boolean recurring,
            String recurrenceRule,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            LocalDate timetableEffectiveFrom,
            LocalDate timetableEffectiveTo,
            LocalDate targetDate
    ) {

        if (targetDate == null) {
            throw new IllegalArgumentException(
                    "targetDate must not be null"
            );
        }

        return resolveEntry(
                dayOfWeek,
                recurring,
                recurrenceRule,
                effectiveFrom,
                effectiveTo,
                timetableEffectiveFrom,
                timetableEffectiveTo
        ).contains(
                targetDate
        );
    }

    private List<LocalDate> resolveWeeklyRange(
            DayOfWeek requiredDay,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        List<LocalDate> occurrences =
                new ArrayList<>();

        LocalDate cursor =
                effectiveFrom;

        while (!cursor.isAfter(effectiveTo)) {

            if (
                    cursor.getDayOfWeek()
                            == requiredDay
            ) {
                occurrences.add(
                        cursor
                );
            }

            cursor =
                    cursor.plusDays(1);
        }

        if (occurrences.isEmpty()) {
            throw new IllegalArgumentException(
                    "No timetable occurrence falls within effective date range"
            );
        }

        return List.copyOf(
                occurrences
        );
    }

    private DayOfWeek requireDay(
            String dayOfWeek
    ) {

        if (
                dayOfWeek == null
                || dayOfWeek.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "dayOfWeek must not be blank"
            );
        }

        String normalizedDay =
                dayOfWeek
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        try {

            return DayOfWeek.valueOf(
                    normalizedDay
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid day of week: "
                            + normalizedDay,
                    exception
            );
        }
    }
}
