package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read-only Today Programme projection over the existing academic
 * calendar domain.
 *
 * Calendar recurrence_rule is currently reserved/unimplemented by
 * the academic-calendar write path. This repository therefore reads
 * only materialised persisted event intervals and does not invent
 * recurrence expansion semantics.
 */
public interface TeacherProgrammeCalendarRepository
        extends Repository<AcademicCalendarEvent, UUID> {

    /**
     * Returns displayable academic-calendar events overlapping the
     * authenticated school's current local day.
     *
     * Interval semantics:
     *
     * 1. [startAt, endAt) events overlap when they begin before the
     *    day ends and finish after the day begins.
     *
     * 2. A null endAt is treated conservatively as an event anchored
     *    at startAt, rather than as an indefinitely continuing event.
     *
     * 3. A zero-duration event (endAt == startAt) is likewise treated
     *    as an occurrence anchored at startAt.
     *
     * 4. endExclusive is never included.
     *
     * Lifecycle visibility deliberately excludes DRAFT, POSTPONED,
     * CANCELLED and ARCHIVED events. COMPLETED remains visible because
     * an event completed earlier today is still part of today's
     * programme/history.
     */
    @Query("""
            select e
            from AcademicCalendarEvent e
            where e.tenantId = :tenantId
              and e.eventStatus in (
                    'SCHEDULED',
                    'CONFIRMED',
                    'IN_PROGRESS',
                    'COMPLETED'
              )
              and e.startAt < :endExclusive
              and (
                    (
                        e.endAt is null
                        and e.startAt >= :startInclusive
                    )
                    or
                    (
                        e.endAt is not null
                        and e.endAt = e.startAt
                        and e.startAt >= :startInclusive
                    )
                    or
                    (
                        e.endAt is not null
                        and e.endAt > e.startAt
                        and e.endAt > :startInclusive
                    )
              )
            order by e.startAt, e.id
            """)
    List<AcademicCalendarEvent> findCurrentDayEvents(
            @Param("tenantId") UUID tenantId,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );
}
