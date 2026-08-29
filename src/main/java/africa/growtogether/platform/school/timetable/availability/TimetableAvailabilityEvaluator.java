package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.core.Timetable;
import africa.growtogether.platform.school.timetable.entry.CreateTimetableEntryCommand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimetableAvailabilityEvaluator {

    private final SchoolProfileService schoolProfiles;

    private final TeacherUnavailabilityRepository teacherUnavailability;

    private final ResourceUnavailabilityRepository resourceUnavailability;

    public TimetableAvailabilityEvaluator(
            SchoolProfileService schoolProfiles,
            TeacherUnavailabilityRepository teacherUnavailability,
            ResourceUnavailabilityRepository resourceUnavailability
    ) {

        this.schoolProfiles = schoolProfiles;
        this.teacherUnavailability = teacherUnavailability;
        this.resourceUnavailability = resourceUnavailability;
    }

    /*
     * Pure scheduling evaluation.
     *
     * This deliberately does NOT persist TimetableConflict records.
     * Manual entry creation may persist a rejected conflict later,
     * while the automatic generator can evaluate candidate slots
     * without polluting the audit register.
     */
    @Transactional(readOnly = true)
    public Optional<TimetableAvailabilityConflict> evaluate(
            UUID tenantId,
            Timetable timetable,
            BellPeriod bellPeriod,
            CreateTimetableEntryCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (timetable == null) {
            throw new IllegalArgumentException(
                    "timetable must not be null"
            );
        }

        if (bellPeriod == null) {
            throw new IllegalArgumentException(
                    "bellPeriod must not be null"
            );
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "command must not be null"
            );
        }

        ZoneId schoolZone =
                schoolProfiles.requireTimezone(
                        tenantId
                );

        List<LocalDate> occurrenceDates =
                resolveOccurrenceDates(
                        timetable,
                        command
                );

        if (command.teacherProfileId() != null) {

            Optional<TimetableAvailabilityConflict> teacherConflict =
                    evaluateTeacher(
                            tenantId,
                            command.teacherProfileId(),
                            bellPeriod,
                            occurrenceDates,
                            schoolZone
                    );

            if (teacherConflict.isPresent()) {
                return teacherConflict;
            }
        }

        if (command.schedulingResourceId() != null) {

            Optional<TimetableAvailabilityConflict> resourceConflict =
                    evaluateResource(
                            tenantId,
                            command.schedulingResourceId(),
                            bellPeriod,
                            occurrenceDates,
                            schoolZone
                    );

            if (resourceConflict.isPresent()) {
                return resourceConflict;
            }
        }

        return Optional.empty();
    }

    /*
     * IMPROVEMENT — automatic timetable candidate evaluation.
     *
     * Candidate generation happens before a TimetableEntry exists.
     * Requiring a synthetic Timetable merely to evaluate availability
     * would create a false persistence/domain dependency.
     *
     * This entry point therefore evaluates a weekly candidate directly,
     * while deliberately reusing the SAME teacher/resource availability
     * logic, school timezone and unavailability repositories used by the
     * authoritative timetable-entry path.
     *
     * It remains pure/read-only and does NOT persist TimetableConflict
     * evidence for hypothetical candidate slots.
     */
    @Transactional(readOnly = true)
    public Optional<TimetableAvailabilityConflict> evaluateCandidate(
            UUID tenantId,
            BellPeriod bellPeriod,
            String dayOfWeek,
            UUID teacherProfileId,
            UUID schedulingResourceId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (bellPeriod == null) {
            throw new IllegalArgumentException(
                    "bellPeriod must not be null"
            );
        }

        if (
                dayOfWeek == null
                || dayOfWeek.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "dayOfWeek must not be blank"
            );
        }

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

        ZoneId schoolZone =
                schoolProfiles.requireTimezone(
                        tenantId
                );

        List<LocalDate> occurrenceDates =
                resolveCandidateOccurrenceDates(
                        dayOfWeek,
                        effectiveFrom,
                        effectiveTo
                );

        if (teacherProfileId != null) {

            Optional<TimetableAvailabilityConflict> teacherConflict =
                    evaluateTeacher(
                            tenantId,
                            teacherProfileId,
                            bellPeriod,
                            occurrenceDates,
                            schoolZone
                    );

            if (teacherConflict.isPresent()) {
                return teacherConflict;
            }
        }

        if (schedulingResourceId != null) {

            Optional<TimetableAvailabilityConflict> resourceConflict =
                    evaluateResource(
                            tenantId,
                            schedulingResourceId,
                            bellPeriod,
                            occurrenceDates,
                            schoolZone
                    );

            if (resourceConflict.isPresent()) {
                return resourceConflict;
            }
        }

        return Optional.empty();
    }

    private List<LocalDate> resolveCandidateOccurrenceDates(
            String dayOfWeek,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        String normalizedDay =
                dayOfWeek
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        final DayOfWeek requiredDay;

        try {

            requiredDay =
                    DayOfWeek.valueOf(
                            normalizedDay
                    );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid day of week: "
                            + normalizedDay,
                    exception
            );
        }

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

        return occurrences;
    }

    private Optional<TimetableAvailabilityConflict> evaluateTeacher(
            UUID tenantId,
            UUID teacherProfileId,
            BellPeriod bellPeriod,
            List<LocalDate> occurrenceDates,
            ZoneId schoolZone
    ) {

        List<TeacherUnavailability> records =
                teacherUnavailability
                        .findByTenantIdAndTeacherProfileIdAndStatus(
                                tenantId,
                                teacherProfileId,
                                EntityStatus.ACTIVE
                        );

        /*
         * Recurrence is stored by V039, but the repository inspection
         * has not shown a recurrence-rule interpreter yet.
         *
         * Failing explicitly is safer than silently overlooking
         * recurring leave, meetings, training or restrictions.
         */
        if (
                records.stream()
                        .anyMatch(
                                TeacherUnavailability::isRecurring
                        )
        ) {
            throw new IllegalStateException(
                    "Recurring teacher unavailability requires recurrence-rule evaluation"
            );
        }

        for (LocalDate date : occurrenceDates) {

            LessonInterval lesson =
                    lessonInterval(
                            date,
                            bellPeriod,
                            schoolZone
                    );

            for (
                    TeacherUnavailability unavailable
                    : records
            ) {

                if (
                        overlaps(
                                lesson.start(),
                                lesson.end(),
                                unavailable.getUnavailableFrom(),
                                unavailable.getUnavailableTo()
                        )
                ) {

                    return Optional.of(
                            new TimetableAvailabilityConflict(
                                    "TEACHER_UNAVAILABLE",
                                    "Teacher is unavailable on "
                                            + date
                                            + " during "
                                            + bellPeriod.getPeriodCode()
                                            + " ("
                                            + unavailable.getReasonType()
                                            + ")"
                            )
                    );
                }
            }
        }

        return Optional.empty();
    }

    private Optional<TimetableAvailabilityConflict> evaluateResource(
            UUID tenantId,
            UUID resourceId,
            BellPeriod bellPeriod,
            List<LocalDate> occurrenceDates,
            ZoneId schoolZone
    ) {

        List<ResourceUnavailability> records =
                resourceUnavailability
                        .findByTenantIdAndSchedulingResourceIdAndStatus(
                                tenantId,
                                resourceId,
                                EntityStatus.ACTIVE
                        );

        if (
                records.stream()
                        .anyMatch(
                                ResourceUnavailability::isRecurring
                        )
        ) {
            throw new IllegalStateException(
                    "Recurring resource unavailability requires recurrence-rule evaluation"
            );
        }

        for (LocalDate date : occurrenceDates) {

            LessonInterval lesson =
                    lessonInterval(
                            date,
                            bellPeriod,
                            schoolZone
                    );

            for (
                    ResourceUnavailability unavailable
                    : records
            ) {

                if (
                        overlaps(
                                lesson.start(),
                                lesson.end(),
                                unavailable.getUnavailableFrom(),
                                unavailable.getUnavailableTo()
                        )
                ) {

                    return Optional.of(
                            new TimetableAvailabilityConflict(
                                    "RESOURCE_UNAVAILABLE",
                                    "Scheduling resource is unavailable on "
                                            + date
                                            + " during "
                                            + bellPeriod.getPeriodCode()
                                            + " ("
                                            + unavailable.getReasonType()
                                            + ")"
                            )
                    );
                }
            }
        }

        return Optional.empty();
    }

    private List<LocalDate> resolveOccurrenceDates(
            Timetable timetable,
            CreateTimetableEntryCommand command
    ) {

        String normalizedDay =
                command.dayOfWeek()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        final DayOfWeek requiredDay;

        try {

            requiredDay =
                    DayOfWeek.valueOf(
                            normalizedDay
                    );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid day of week: "
                            + normalizedDay,
                    exception
            );
        }

        boolean recurring =
                command.recurring() == null
                        || command.recurring();

        /*
         * Weekly recurrence by the entry's day-of-week is supported.
         *
         * Custom RRULE interpretation is deliberately deferred until
         * a governed recurrence engine is implemented.
         */
        if (
                recurring
                && command.recurrenceRule() != null
                && !command.recurrenceRule().isBlank()
        ) {
            throw new IllegalStateException(
                    "Custom timetable recurrence rules are not yet supported for availability evaluation"
            );
        }

        LocalDate start =
                command.effectiveFrom() == null
                        ? timetable.getEffectiveFrom()
                        : command.effectiveFrom();

        if (start == null) {
            throw new IllegalArgumentException(
                    "Timetable entry effectiveFrom is required for availability evaluation"
            );
        }

        if (!recurring) {

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
                command.effectiveTo() == null
                        ? timetable.getEffectiveTo()
                        : command.effectiveTo();

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

        List<LocalDate> occurrences =
                new ArrayList<>();

        LocalDate cursor =
                start;

        while (!cursor.isAfter(end)) {

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

        return occurrences;
    }

    private LessonInterval lessonInterval(
            LocalDate date,
            BellPeriod bellPeriod,
            ZoneId schoolZone
    ) {

        ZonedDateTime localStart =
                ZonedDateTime.of(
                        date,
                        bellPeriod.getStartTime(),
                        schoolZone
                );

        ZonedDateTime localEnd =
                ZonedDateTime.of(
                        date,
                        bellPeriod.getEndTime(),
                        schoolZone
                );

        return new LessonInterval(
                localStart.toInstant(),
                localEnd.toInstant()
        );
    }

    private boolean overlaps(
            Instant firstStart,
            Instant firstEnd,
            Instant secondStart,
            Instant secondEnd
    ) {

        return firstStart.isBefore(secondEnd)
                && firstEnd.isAfter(secondStart);
    }

    private record LessonInterval(
            Instant start,
            Instant end
    ) {
    }
}
