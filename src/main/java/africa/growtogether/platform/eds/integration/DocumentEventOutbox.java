package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="eds_document_event_outbox", indexes=@Index(name="idx_eds_outbox_unpublished",columnList="published_at,created_at"))
public class DocumentEventOutbox extends AuditedTenantEntity {
 @Column(name="document_id",nullable=false) private UUID documentId;
 @Column(name="event_type",nullable=false,length=100) private String eventType;
 @Column(name="payload_json",nullable=false,columnDefinition="text") private String payloadJson;
 @Column(name="published_at") private Instant publishedAt;
 protected DocumentEventOutbox(){}
 public DocumentEventOutbox(UUID tenantId,UUID documentId,String eventType,String payloadJson){setTenantId(tenantId);this.documentId=documentId;this.eventType=eventType;this.payloadJson=payloadJson;}
 public UUID id(){return getId();} public UUID documentId(){return documentId;} public String eventType(){return eventType;} public boolean published(){return publishedAt!=null;} public void markPublished(){publishedAt=Instant.now();}
}
