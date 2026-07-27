package africa.growtogether.platform.school.academic.calendar;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public interface AcademicCalendarEventRepository
        extends JpaRepository<AcademicCalendarEvent, UUID> {


    List<AcademicCalendarEvent> findByAcademicYearId(
            UUID academicYearId
    );


    List<AcademicCalendarEvent> findByAcademicTermId(
            UUID academicTermId
    );


    List<AcademicCalendarEvent> findByEventStatus(
            String eventStatus
    );


    List<AcademicCalendarEvent> findByStartAtBetween(
            Instant start,
            Instant end
    );

}
