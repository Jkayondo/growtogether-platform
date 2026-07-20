package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="eds_workflow_document_links", uniqueConstraints=@UniqueConstraint(name="uk_eds_workflow_document_link", columnNames={"tenant_id","workflow_instance_id","document_id","relationship_type"}))
public class WorkflowDocumentLink extends AuditedTenantEntity {
    @Column(name="workflow_instance_id",nullable=false) private UUID workflowInstanceId;
    @Column(name="document_id",nullable=false) private UUID documentId;
    @Column(name="relationship_type",nullable=false,length=40) private String relationshipType;
    protected WorkflowDocumentLink() {}
    public WorkflowDocumentLink(UUID tenantId, UUID workflowInstanceId, UUID documentId, String relationshipType) {
        setTenantId(tenantId); this.workflowInstanceId=workflowInstanceId; this.documentId=documentId;
        this.relationshipType=(relationshipType==null||relationshipType.isBlank()?"ATTACHMENT":relationshipType.trim().toUpperCase());
    }
    public UUID id(){return getId();} public UUID workflowInstanceId(){return workflowInstanceId;} public UUID documentId(){return documentId;} public String relationshipType(){return relationshipType;}
}
