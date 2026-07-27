package africa.growtogether.platform.school.academic.calendar;


import africa.growtogether.platform.common.events.EventPublisher;
import africa.growtogether.platform.school.academic.calendar.events.AcademicCalendarEventCreatedEvent;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.year.AcademicYear;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class AcademicCalendarEventService {


    private final AcademicCalendarEventRepository repository;
    private final EventPublisher eventPublisher;


    public AcademicCalendarEventService(
            AcademicCalendarEventRepository repository,
            EventPublisher eventPublisher
    ) {
         this.repository = repository;
         this.eventPublisher = eventPublisher;
     }


    @Transactional
    public AcademicCalendarEvent create(
            UUID tenantId,
            AcademicYear academicYear,
            AcademicTerm academicTerm,
            String eventCode,
            String eventName,
            String eventType,
            Instant startAt,
            Instant endAt
    ) {


        validateDates(
                startAt,
                endAt
        );


        AcademicCalendarEvent event =
                new AcademicCalendarEvent(
                        academicYear,
                        academicTerm,
                        eventCode,
                        eventName,
                        eventType,
                        startAt
                );


        event.setTenantId(
                tenantId
        );


        AcademicCalendarEvent saved =
                repository.save(event);


        eventPublisher.publish(
                new AcademicCalendarEventCreatedEvent(
                        saved.getId(),
                        tenantId,
                        saved.getEventType(),
                        saved.getEventName(),
                        saved.getStartAt(),
                        saved.isNotificationRequired(),
                        Instant.now()
                )
        );


        return saved;
    }

    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findByAcademicYear(
            UUID academicYearId
    ) {

        return repository.findByAcademicYearId(
                academicYearId
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findByAcademicTerm(
            UUID academicTermId
    ) {

        return repository.findByAcademicTermId(
                academicTermId
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findScheduledEvents() {

        return repository.findByEventStatus(
                "SCHEDULED"
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findUpcomingEvents(
            Instant start,
            Instant end
    ) {

        return repository.findByStartAtBetween(
                start,
                end
        );
    }


    private void validateDates(
            Instant startAt,
            Instant endAt
    ) {

        if (endAt != null
                && endAt.isBefore(startAt)) {

            throw new IllegalArgumentException(
                    "Calendar event end time must be after start time"
            );
        }
    }

}