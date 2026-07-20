package africa.growtogether.platform.ewe;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID; import org.hibernate.annotations.UuidGenerator;
@Entity @Table(name="ewe_workflow_execution_events",indexes=@Index(name="ix_ewe_event_instance_time",columnList="tenant_id,instance_id,occurred_at"))
public class WorkflowExecutionEvent {
 @Id @GeneratedValue @UuidGenerator private UUID id;
 @Column(name="tenant_id",nullable=false,updatable=false) private UUID tenantId;
 @Column(name="instance_id",nullable=false,updatable=false) private UUID instanceId;
 @Enumerated(EnumType.STRING) @Column(name="event_type",nullable=false,length=30,updatable=false) private WorkflowEventType eventType;
 @Column(name="step_code",length=160,updatable=false) private String stepCode;
 @Column(name="message",length=1000,updatable=false) private String message;
 @Column(name="details",columnDefinition="jsonb",updatable=false) private String details;
 @Column(name="occurred_at",nullable=false,updatable=false) private Instant occurredAt;
 protected WorkflowExecutionEvent(){}
 public WorkflowExecutionEvent(UUID tenantId,UUID instanceId,WorkflowEventType type,String step,String message,String details){this.tenantId=tenantId;this.instanceId=instanceId;this.eventType=type;this.stepCode=step;this.message=message;this.details=(details==null||details.isBlank())?"{}":details;this.occurredAt=Instant.now();}
 public UUID getId(){return id;} public UUID getTenantId(){return tenantId;} public UUID getInstanceId(){return instanceId;} public WorkflowEventType getEventType(){return eventType;} public String getStepCode(){return stepCode;} public String getMessage(){return message;} public String getDetails(){return details;} public Instant getOccurredAt(){return occurredAt;}
}
