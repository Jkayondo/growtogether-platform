package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="ens_notification_requests", indexes={
 @Index(name="ix_ens_request_tenant_status", columnList="tenant_id,notification_status"),
 @Index(name="ix_ens_request_correlation", columnList="correlation_id")})
public class NotificationRequest extends AuditedTenantEntity {
 @Column(name="definition_code", nullable=false, length=120) private String definitionCode;
 @Column(name="recipient", nullable=false, length=320) private String recipient;
 @Enumerated(EnumType.STRING) @Column(name="channel", nullable=false, length=20) private NotificationChannel channel;
 @Enumerated(EnumType.STRING) @Column(name="priority", nullable=false, length=20) private NotificationPriority priority;
 @Enumerated(EnumType.STRING) @Column(name="notification_status", nullable=false, length=24) private NotificationStatus notificationStatus=NotificationStatus.CREATED;
 @Column(name="subject", length=300) private String subject;
 @Column(name="body", nullable=false, columnDefinition="text") private String body;
 @Column(name="correlation_id", length=100) private String correlationId;
 @Column(name="source_service", nullable=false, length=80) private String sourceService;
 @Column(name="source_reference", length=160) private String sourceReference;
 @Column(name="attempt_count", nullable=false) private int attemptCount;
 @Column(name="next_attempt_at") private Instant nextAttemptAt;
 @Column(name="provider_reference", length=200) private String providerReference;
 @Column(name="last_error", columnDefinition="text") private String lastError;
 protected NotificationRequest() {}
 public NotificationRequest(UUID tenantId,String definitionCode,String recipient,NotificationChannel channel,NotificationPriority priority,String subject,String body,String correlationId,String sourceService,String sourceReference){
  setTenantId(tenantId); this.definitionCode=required(definitionCode,"definitionCode").toUpperCase(); this.recipient=required(recipient,"recipient"); this.channel=channel; this.priority=priority==null?NotificationPriority.NORMAL:priority; this.subject=subject; this.body=required(body,"body"); this.correlationId=correlationId; this.sourceService=required(sourceService,"sourceService"); this.sourceReference=sourceReference;
 }
 private static String required(String v,String n){if(v==null||v.isBlank()) throw new IllegalArgumentException(n+" is required"); return v.trim();}
 public void queue(){require(NotificationStatus.CREATED); notificationStatus=NotificationStatus.QUEUED;}
 public void processing(){if(notificationStatus!=NotificationStatus.QUEUED&&notificationStatus!=NotificationStatus.RETRYING) throw new IllegalStateException("Notification is not dispatchable"); notificationStatus=NotificationStatus.PROCESSING; attemptCount++;}
 public void sent(String ref){require(NotificationStatus.PROCESSING); notificationStatus=NotificationStatus.SENT; providerReference=ref; lastError=null; nextAttemptAt=null;}
 public void delivered(){if(notificationStatus!=NotificationStatus.SENT) throw new IllegalStateException("Only sent notifications can be delivered"); notificationStatus=NotificationStatus.DELIVERED;}
 public void fail(String error, Instant retryAt, int maxAttempts){if(notificationStatus!=NotificationStatus.PROCESSING) throw new IllegalStateException("Only processing notifications can fail"); lastError=error; if(attemptCount>=maxAttempts){notificationStatus=NotificationStatus.DEAD_LETTER; nextAttemptAt=null;}else{notificationStatus=NotificationStatus.RETRYING; nextAttemptAt=retryAt;}}
 private void require(NotificationStatus expected){if(notificationStatus!=expected) throw new IllegalStateException("Expected "+expected+" but was "+notificationStatus);}
 public UUID id(){return getId();} public String definitionCode(){return definitionCode;} public String recipient(){return recipient;} public NotificationChannel channel(){return channel;} public NotificationPriority priority(){return priority;} public NotificationStatus notificationStatus(){return notificationStatus;} public String subject(){return subject;} public String body(){return body;} public String correlationId(){return correlationId;} public String sourceService(){return sourceService;} public String sourceReference(){return sourceReference;} public int attemptCount(){return attemptCount;} public Instant nextAttemptAt(){return nextAttemptAt;} public String providerReference(){return providerReference;} public String lastError(){return lastError;}
}
