package africa.growtogether.platform.eap;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="eap_events", indexes={
 @Index(name="ix_eap_event_tenant_status", columnList="tenant_id,processing_status,event_time"),
 @Index(name="ix_eap_event_type_time", columnList="tenant_id,event_type,event_time"),
 @Index(name="ix_eap_event_source", columnList="tenant_id,source_service,event_time")})
public class AnalyticsEvent extends AuditedTenantEntity {
 @Column(name="event_type", nullable=false, length=160) private String eventType;
 @Column(name="source_service", nullable=false, length=100) private String sourceService;
 @Column(name="correlation_id", length=100) private String correlationId;
 @Column(name="event_time", nullable=false) private Instant eventTime;
 @Column(name="payload_json", nullable=false, columnDefinition="text") private String payloadJson;
 @Enumerated(EnumType.STRING) @Column(name="processing_status", nullable=false, length=30) private AnalyticsEnums.EventStatus processingStatus=AnalyticsEnums.EventStatus.RECEIVED;
 @Column(name="attempt_count", nullable=false) private int attemptCount;
 @Column(name="last_error", columnDefinition="text") private String lastError;
 protected AnalyticsEvent() {}
 public AnalyticsEvent(java.util.UUID tenantId,String eventType,String sourceService,String correlationId,Instant eventTime,String payloadJson){setTenantId(tenantId);this.eventType=req(eventType);this.sourceService=req(sourceService);this.correlationId=correlationId;this.eventTime=eventTime==null?Instant.now():eventTime;this.payloadJson=req(payloadJson);}
 private static String req(String value){if(value==null||value.isBlank())throw new IllegalArgumentException("value is required");return value.trim();}
 public void begin(){if(processingStatus!=AnalyticsEnums.EventStatus.RECEIVED&&processingStatus!=AnalyticsEnums.EventStatus.FAILED)throw new IllegalStateException("Event cannot be processed");processingStatus=AnalyticsEnums.EventStatus.PROCESSING;attemptCount++;}
 public void complete(){if(processingStatus!=AnalyticsEnums.EventStatus.PROCESSING)throw new IllegalStateException("Event is not processing");processingStatus=AnalyticsEnums.EventStatus.PROCESSED;lastError=null;}
 public void fail(String error,int maxAttempts){if(processingStatus!=AnalyticsEnums.EventStatus.PROCESSING)throw new IllegalStateException("Event is not processing");lastError=req(error);processingStatus=attemptCount>=maxAttempts?AnalyticsEnums.EventStatus.DEAD_LETTER:AnalyticsEnums.EventStatus.FAILED;}
 public String eventType(){return eventType;} public String sourceService(){return sourceService;} public Instant eventTime(){return eventTime;} public String payloadJson(){return payloadJson;} public AnalyticsEnums.EventStatus processingStatus(){return processingStatus;} public int attemptCount(){return attemptCount;}
}
