package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="eds_document_ai_requests")
public class DocumentAiRequest extends AuditedTenantEntity {
 @Column(name="document_id",nullable=false) private UUID documentId;
 @Column(name="operation",nullable=false,length=50) private String operation;
 @Column(name="request_status",nullable=false,length=24) private String requestStatus="QUEUED";
 protected DocumentAiRequest(){}
 public DocumentAiRequest(UUID tenantId,UUID documentId,String operation){setTenantId(tenantId);this.documentId=documentId;this.operation=operation.trim().toUpperCase();}
 public UUID id(){return getId();} public UUID documentId(){return documentId;} public String operation(){return operation;} public String requestStatus(){return requestStatus;}
}
