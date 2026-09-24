package africa.growtogether.platform.school.teacher.programme;

import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEvent;
import africa.growtogether.platform.school.programme.ProgrammeCalendarRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Compatibility adapter preserving the historical Teacher programme
 * repository contract while query ownership resides in the shared
 * school programme capability.
 */
@Repository
public class TeacherProgrammeCalendarRepository {

    private final ProgrammeCalendarRepository calendar;

    public TeacherProgrammeCalendarRepository(
            ProgrammeCalendarRepository calendar
    ) {
        this.calendar = calendar;
    }

    public List<AcademicCalendarEvent> findCurrentDayEvents(
            UUID tenantId,
            Instant startInclusive,
            Instant endExclusive
    ) {
        return calendar.findCurrentDayEvents(
                tenantId,
                startInclusive,
                endExclusive
        );
    }
}
