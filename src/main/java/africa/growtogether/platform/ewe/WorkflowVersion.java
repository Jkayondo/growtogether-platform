package africa.growtogether.platform.ewe;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="ewe_workflow_versions",uniqueConstraints=@UniqueConstraint(name="uk_ewe_version_definition_number",columnNames={"tenant_id","definition_id","version_number"}))
public class WorkflowVersion extends AuditedTenantEntity {
 @Column(name="definition_id",nullable=false,updatable=false) private UUID definitionId;
 @Column(name="version_number",nullable=false,updatable=false) private int versionNumber;
 @Column(name="definition_json",nullable=false,columnDefinition="jsonb",updatable=false) private String definitionJson;
 @Column(name="checksum",nullable=false,length=64,updatable=false) private String checksum;
 @Column(name="published",nullable=false) private boolean published;
 protected WorkflowVersion(){}
 public WorkflowVersion(UUID definitionId,int versionNumber,String definitionJson,String checksum){if(versionNumber<1)throw new WorkflowException("Workflow version must be positive.");this.definitionId=definitionId;this.versionNumber=versionNumber;this.definitionJson=WorkflowDefinition.require(definitionJson,"definitionJson");this.checksum=WorkflowDefinition.require(checksum,"checksum");}
 public void publish(){published=true;}
 public UUID getDefinitionId(){return definitionId;} public int getVersionNumber(){return versionNumber;} public String getDefinitionJson(){return definitionJson;} public String getChecksum(){return checksum;} public boolean isPublished(){return published;}
}
