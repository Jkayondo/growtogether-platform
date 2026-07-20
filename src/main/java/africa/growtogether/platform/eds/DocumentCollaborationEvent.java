package africa.growtogether.platform.eds;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="eds_document_collaboration_events",indexes=@Index(name="ix_eds_collab_document",columnList="tenant_id,document_id,occurred_at"))
public class DocumentCollaborationEvent extends AuditedTenantEntity {
 @Column(name="document_id",nullable=false) private UUID documentId;
 @Column(name="event_type",nullable=false,length=48) private String eventType;
 @Column(name="actor_user_id") private UUID actorUserId;
 @Column(columnDefinition="text") private String details;
 @Column(name="occurred_at",nullable=false) private Instant occurredAt;
 protected DocumentCollaborationEvent(){}
 public DocumentCollaborationEvent(UUID tenantId,UUID documentId,String eventType,UUID actorUserId,String details){setTenantId(tenantId);this.documentId=documentId;this.eventType=eventType;this.actorUserId=actorUserId;this.details=details;this.occurredAt=Instant.now();}
}
