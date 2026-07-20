package africa.growtogether.platform.eiam.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name="eiam_audit_events", indexes={
 @Index(name="ix_audit_tenant_occurred", columnList="tenant_id,occurred_at"),
 @Index(name="ix_audit_actor", columnList="tenant_id,actor_user_id,occurred_at"),
 @Index(name="ix_audit_event_type", columnList="tenant_id,event_type,occurred_at"),
 @Index(name="ix_audit_correlation", columnList="correlation_id")})
public class AuditEvent {
 @Id @GeneratedValue @UuidGenerator @Column(nullable=false,updatable=false) private UUID id;
 @Column(name="tenant_id",nullable=false,updatable=false) private UUID tenantId;
 @Column(name="actor_user_id",updatable=false) private UUID actorUserId;
 @Column(name="actor_username",length=150,updatable=false) private String actorUsername;
 @Column(name="event_type",nullable=false,length=120,updatable=false) private String eventType;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=40,updatable=false) private AuditEventCategory category;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20,updatable=false) private AuditOutcome outcome;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20,updatable=false) private SecuritySeverity severity;
 @Column(name="resource_type",length=100,updatable=false) private String resourceType;
 @Column(name="resource_id",length=100,updatable=false) private String resourceId;
 @Column(name="source_ip",length=64,updatable=false) private String sourceIp;
 @Column(name="user_agent",length=512,updatable=false) private String userAgent;
 @Column(name="correlation_id",nullable=false,length=128,updatable=false) private String correlationId;
 @Column(name="session_id",updatable=false) private UUID sessionId;
 @Column(name="message",nullable=false,length=500,updatable=false) private String message;
 @Column(name="details_json",columnDefinition="text",updatable=false) private String detailsJson;
 @Column(name="occurred_at",nullable=false,updatable=false) private Instant occurredAt;
 protected AuditEvent() {}
 public AuditEvent(UUID tenantId, UUID actorUserId, String actorUsername, String eventType, AuditEventCategory category, AuditOutcome outcome, SecuritySeverity severity, String resourceType, String resourceId, String sourceIp, String userAgent, String correlationId, UUID sessionId, String message, String detailsJson, Instant occurredAt) {
  this.tenantId=tenantId; this.actorUserId=actorUserId; this.actorUsername=actorUsername; this.eventType=eventType; this.category=category; this.outcome=outcome; this.severity=severity; this.resourceType=resourceType; this.resourceId=resourceId; this.sourceIp=sourceIp; this.userAgent=userAgent; this.correlationId=correlationId; this.sessionId=sessionId; this.message=message; this.detailsJson=detailsJson; this.occurredAt=occurredAt;
 }
 public UUID getId(){return id;} public UUID getTenantId(){return tenantId;} public UUID getActorUserId(){return actorUserId;} public String getActorUsername(){return actorUsername;} public String getEventType(){return eventType;} public AuditEventCategory getCategory(){return category;} public AuditOutcome getOutcome(){return outcome;} public SecuritySeverity getSeverity(){return severity;} public String getResourceType(){return resourceType;} public String getResourceId(){return resourceId;} public String getSourceIp(){return sourceIp;} public String getCorrelationId(){return correlationId;} public UUID getSessionId(){return sessionId;} public String getMessage(){return message;} public String getDetailsJson(){return detailsJson;} public Instant getOccurredAt(){return occurredAt;}
 @PreUpdate @PreRemove private void rejectMutation(){throw new IllegalStateException("Audit events are immutable.");}
}
