package africa.growtogether.platform.eds;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="eds_document_lifecycle_events",indexes=@Index(name="ix_eds_event_document",columnList="tenant_id,document_id,occurred_at"))
public class DocumentLifecycleEvent extends AuditedTenantEntity {
 @Column(name="document_id",nullable=false) private UUID documentId;
 @Column(name="event_type",nullable=false,length=50) private String eventType;
 @Column(name="details",columnDefinition="text") private String details;
 @Column(name="occurred_at",nullable=false) private Instant occurredAt;
 protected DocumentLifecycleEvent(){}
 public DocumentLifecycleEvent(UUID tenantId,UUID documentId,String eventType,String details,Instant occurredAt){setTenantId(tenantId);this.documentId=documentId;this.eventType=eventType;this.details=details;this.occurredAt=occurredAt;}
}
