package africa.growtogether.platform.school.academic.calendar;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.year.AcademicYear;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_academic_calendar_event")
public class AcademicCalendarEvent extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(
            name = "academic_year_id",
            nullable = false
    )
    private AcademicYear academicYear;


    @ManyToOne
    @JoinColumn(
            name = "academic_term_id"
    )
    private AcademicTerm academicTerm;


    @Column(
            name = "campus_id"
    )
    private UUID campusId;


    @Column(
            name = "event_code",
            nullable = false,
            length = 100
    )
    private String eventCode;


    @Column(
            name = "event_name",
            nullable = false,
            length = 250
    )
    private String eventName;


    @Column(
            name = "event_description",
            length = 1500
    )
    private String eventDescription;


    @Column(
            name = "event_type",
            nullable = false,
            length = 40
    )
    private String eventType;


    @Column(
            name = "start_at",
            nullable = false
    )
    private Instant startAt;


    @Column(
            name = "end_at"
    )
    private Instant endAt;


    @Column(
            name = "all_day",
            nullable = false
    )
    private boolean allDay;


    @Column(
            name = "instructional_day",
            nullable = false
    )
    private boolean instructionalDay;


    @Column(
            name = "institution_closed",
            nullable = false
    )
    private boolean institutionClosed;


    @Column(
            name = "recurrence_rule",
            length = 500
    )
    private String recurrenceRule;


    @Column(
            name = "notification_required",
            nullable = false
    )
    private boolean notificationRequired;


    @Column(
            name = "eds_document_id"
    )
    private UUID edsDocumentId;


    @Column(
            name = "workflow_instance_id"
    )
    private UUID workflowInstanceId;


    @Column(
            name = "event_status",
            nullable = false,
            length = 30
    )
    private String eventStatus;


    protected AcademicCalendarEvent() {
    }


    public AcademicCalendarEvent(
            AcademicYear academicYear,
            AcademicTerm academicTerm,
            String eventCode,
            String eventName,
            String eventType,
            Instant startAt
    ) {

        this.academicYear = academicYear;
        this.academicTerm = academicTerm;
        this.eventCode = eventCode;
        this.eventName = eventName;
        this.eventType = eventType;
        this.startAt = startAt;
        this.eventStatus = "SCHEDULED";
    }


    public AcademicYear getAcademicYear() {
        return academicYear;
    }


    public AcademicTerm getAcademicTerm() {
        return academicTerm;
    }


    public String getEventCode() {
        return eventCode;
    }


    public String getEventName() {
        return eventName;
    }


    public String getEventType() {
        return eventType;
    }


    public Instant getStartAt() {
        return startAt;
    }


    public String getEventStatus() {
        return eventStatus;
    }

    public boolean isNotificationRequired() {
        return notificationRequired;
    }

public void updateStatus(
        String newStatus
) {

    if (!isValidTransition(
            this.eventStatus,
            newStatus
    )) {

        throw new IllegalArgumentException(
                "Invalid academic calendar event status transition from "
                        + this.eventStatus
                        + " to "
                        + newStatus
        );
    }


    this.eventStatus = newStatus;
}


private boolean isValidTransition(
        String current,
        String next
) {

    return switch (current) {

        case "DRAFT" ->
                next.equals("SCHEDULED")
                || next.equals("CANCELLED");

        case "SCHEDULED" ->
                next.equals("CONFIRMED")
                || next.equals("POSTPONED")
                || next.equals("CANCELLED");

        case "CONFIRMED" ->
                next.equals("IN_PROGRESS")
                || next.equals("CANCELLED")
                || next.equals("POSTPONED");

        case "IN_PROGRESS" ->
                next.equals("COMPLETED");

        case "COMPLETED" ->
                next.equals("ARCHIVED");

        case "POSTPONED" ->
                next.equals("SCHEDULED")
                || next.equals("CANCELLED");

        case "CANCELLED" ->
                next.equals("ARCHIVED");

        default ->
                false;
    };
}

}
