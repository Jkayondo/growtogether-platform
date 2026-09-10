package africa.growtogether.platform.school.academic.calendar;


import africa.growtogether.platform.common.events.EventPublisher;
import africa.growtogether.platform.school.academic.calendar.events.AcademicCalendarEventCreatedEvent;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.year.AcademicYear;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class AcademicCalendarEventService {


    private final AcademicCalendarEventRepository repository;
    private final EventPublisher eventPublisher;
    private final EnterpriseIdentityContext identity;


    public AcademicCalendarEventService(
            AcademicCalendarEventRepository repository,
            EventPublisher eventPublisher,
            EnterpriseIdentityContext identity
    ) {
         this.repository = repository;
         this.eventPublisher = eventPublisher;
         this.identity = identity;
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


        identity.requireTenant(tenantId);
        if (academicYear == null
                || !tenantId.equals(academicYear.getTenantId())) {
            throw new AccessDeniedException("Academic year belongs to another tenant.");
        }
        if (academicTerm != null) {
            if (!tenantId.equals(academicTerm.getTenantId())
                    || academicTerm.getAcademicYear() == null
                    || !tenantId.equals(academicTerm.getAcademicYear().getTenantId())) {
                throw new AccessDeniedException("Academic term belongs to another tenant.");
            }
            if (academicYear.getId() == null
                    || !academicYear.getId().equals(academicTerm.getAcademicYear().getId())) {
                throw new IllegalArgumentException("Academic term does not belong to the year.");
            }
        }

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


        event.setEndAt(endAt);

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

        return repository.findByTenantIdAndAcademicYearId(
                identity.requireTenantId(),
                academicYearId
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findByAcademicTerm(
            UUID academicTermId
    ) {

        return repository.findByTenantIdAndAcademicTermId(
                identity.requireTenantId(),
                academicTermId
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findScheduledEvents() {

        return repository.findByTenantIdAndEventStatus(
                identity.requireTenantId(),
                "SCHEDULED"
        );
    }


    @Transactional(readOnly = true)
    public List<AcademicCalendarEvent> findUpcomingEvents(
            Instant start,
            Instant end
    ) {

        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("A valid calendar query interval is required.");
        }
        return repository.findByTenantIdAndStartAtBetween(
                identity.requireTenantId(),
                start,
                end
        );
    }


    private void validateDates(
            Instant startAt,
            Instant endAt
    ) {

        if (startAt == null) {
            throw new IllegalArgumentException("Calendar start time is required.");
        }
        if (endAt != null
                && endAt.isBefore(startAt)) {

            throw new IllegalArgumentException(
                    "Calendar event end time must be after start time"
            );
        }
    }

}