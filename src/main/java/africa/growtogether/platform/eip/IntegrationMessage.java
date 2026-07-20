package africa.growtogether.platform.eip;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="eip_messages", indexes={
 @Index(name="ix_eip_message_tenant_status", columnList="tenant_id,message_status,next_attempt_at"),
 @Index(name="ix_eip_message_event", columnList="tenant_id,event_type,created_at"),
 @Index(name="ix_eip_message_idempotency", columnList="tenant_id,idempotency_key", unique=true)})
public class IntegrationMessage extends AuditedTenantEntity {
 @Column(name="event_type", nullable=false, length=160) private String eventType;
 @Column(name="event_version", nullable=false, length=30) private String eventVersion;
 @Column(name="source_service", nullable=false, length=100) private String sourceService;
 @Column(name="destination", nullable=false, length=200) private String destination;
 @Enumerated(EnumType.STRING) @Column(name="protocol", nullable=false, length=30) private IntegrationProtocol protocol;
 @Column(name="payload", nullable=false, columnDefinition="text") private String payload;
 @Column(name="headers_json", columnDefinition="text") private String headersJson;
 @Column(name="correlation_id", length=100) private String correlationId;
 @Column(name="idempotency_key", nullable=false, length=180) private String idempotencyKey;
 @Enumerated(EnumType.STRING) @Column(name="message_status", nullable=false, length=30) private IntegrationMessageStatus messageStatus=IntegrationMessageStatus.PENDING;
 @Column(name="attempt_count", nullable=false) private int attemptCount;
 @Column(name="max_attempts", nullable=false) private int maxAttempts;
 @Column(name="next_attempt_at") private Instant nextAttemptAt;
 @Column(name="delivered_at") private Instant deliveredAt;
 @Column(name="last_error", columnDefinition="text") private String lastError;
 @Column(name="replay_of_message_id") private UUID replayOfMessageId;
 protected IntegrationMessage() {}
 public IntegrationMessage(UUID tenantId,String eventType,String eventVersion,String sourceService,String destination,IntegrationProtocol protocol,String payload,String headersJson,String correlationId,String idempotencyKey,int maxAttempts){
  setTenantId(tenantId); this.eventType=req(eventType,"eventType"); this.eventVersion=req(eventVersion,"eventVersion"); this.sourceService=req(sourceService,"sourceService"); this.destination=req(destination,"destination"); this.protocol=protocol==null?IntegrationProtocol.INTERNAL_EVENT:protocol; this.payload=req(payload,"payload"); this.headersJson=headersJson; this.correlationId=correlationId; this.idempotencyKey=req(idempotencyKey,"idempotencyKey"); if(maxAttempts<1) throw new IllegalArgumentException("maxAttempts must be positive"); this.maxAttempts=maxAttempts;
 }
 private static String req(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
 public void route(){require(IntegrationMessageStatus.PENDING);messageStatus=IntegrationMessageStatus.ROUTED;}
 public void dispatch(){if(messageStatus!=IntegrationMessageStatus.ROUTED&&messageStatus!=IntegrationMessageStatus.RETRYING)throw new IllegalStateException("Message is not dispatchable");messageStatus=IntegrationMessageStatus.DISPATCHING;attemptCount++;nextAttemptAt=null;}
 public void delivered(){require(IntegrationMessageStatus.DISPATCHING);messageStatus=IntegrationMessageStatus.DELIVERED;deliveredAt=Instant.now();lastError=null;}
 public void fail(String error,Instant retryAt){require(IntegrationMessageStatus.DISPATCHING);lastError=req(error,"error");if(attemptCount>=maxAttempts){messageStatus=IntegrationMessageStatus.DEAD_LETTER;nextAttemptAt=null;}else{messageStatus=IntegrationMessageStatus.RETRYING;nextAttemptAt=retryAt;}}
 public IntegrationMessage replay(String newIdempotencyKey){if(messageStatus!=IntegrationMessageStatus.DEAD_LETTER&&messageStatus!=IntegrationMessageStatus.DELIVERED)throw new IllegalStateException("Only delivered or dead-letter messages can be replayed");IntegrationMessage copy=new IntegrationMessage(getTenantId(),eventType,eventVersion,sourceService,destination,protocol,payload,headersJson,correlationId,newIdempotencyKey,maxAttempts);copy.replayOfMessageId=getId();return copy;}
 private void require(IntegrationMessageStatus expected){if(messageStatus!=expected)throw new IllegalStateException("Expected "+expected+" but was "+messageStatus);}
 public UUID id(){return getId();} public String eventType(){return eventType;} public String eventVersion(){return eventVersion;} public String sourceService(){return sourceService;} public String destination(){return destination;} public IntegrationProtocol protocol(){return protocol;} public String payload(){return payload;} public String correlationId(){return correlationId;} public String idempotencyKey(){return idempotencyKey;} public IntegrationMessageStatus messageStatus(){return messageStatus;} public int attemptCount(){return attemptCount;} public int maxAttempts(){return maxAttempts;} public Instant nextAttemptAt(){return nextAttemptAt;} public Instant deliveredAt(){return deliveredAt;} public String lastError(){return lastError;} public UUID replayOfMessageId(){return replayOfMessageId;}
}
