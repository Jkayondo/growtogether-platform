package africa.growtogether.platform.school.results.audit;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name="gts_academic_result_audit_event")
public class AcademicResultAuditEvent
        extends AuditedTenantEntity {


    @Column(name="learner_id")
    private UUID learnerId;


    @Column(name="student_subject_result_id")
    private UUID studentSubjectResultId;


    @Column(name="report_card_id")
    private UUID reportCardId;


    @Column(
            name="event_type",
            nullable=false,
            length=50
    )
    private String eventType;


    @Column(
            name="previous_value",
            columnDefinition="jsonb"
    )
    private String previousValue;


    @Column(
            name="new_value",
            columnDefinition="jsonb"
    )
    private String newValue;


    @Column(
            name="event_reason",
            length=1000
    )
    private String eventReason;


    @Column(
            name="performed_by",
            nullable=false
    )
    private UUID performedBy;


    @Column(
            name="performed_at",
            nullable=false
    )
    private Instant performedAt;



    protected AcademicResultAuditEvent(){
    }



    public AcademicResultAuditEvent(

            UUID learnerId,

            UUID studentSubjectResultId,

            String eventType,

            UUID performedBy

    ){

        this.learnerId = learnerId;
        this.studentSubjectResultId = studentSubjectResultId;
        this.eventType = eventType;
        this.performedBy = performedBy;
        this.performedAt = Instant.now();

    }



    public String getEventType(){

        return eventType;

    }

}
