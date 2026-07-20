package africa.growtogether.platform.ewe;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.util.*;
@Entity @Table(name="ewe_workflow_definitions",uniqueConstraints=@UniqueConstraint(name="uk_ewe_definition_tenant_code",columnNames={"tenant_id","code"}))
public class WorkflowDefinition extends AuditedTenantEntity {
 @Column(nullable=false,length=120) private String code;
 @Column(nullable=false,length=180) private String name;
 @Column(nullable=false,length=80) private String category;
 @Column(length=1000) private String description;
 @Enumerated(EnumType.STRING) @Column(name="definition_status",nullable=false,length=20) private WorkflowDefinitionStatus definitionStatus=WorkflowDefinitionStatus.DRAFT;
 @Column(name="active_version") private Integer activeVersion;
 protected WorkflowDefinition(){}
 public WorkflowDefinition(String code,String name,String category,String description){updateMetadata(code,name,category,description);}
 public void updateMetadata(String code,String name,String category,String description){if(definitionStatus==WorkflowDefinitionStatus.ARCHIVED)throw new WorkflowException("Archived workflow definitions cannot be modified.");this.code=normalize(code);this.name=require(name,"name");this.category=normalize(category);this.description=description;}
 public void activate(int version){if(version<1)throw new WorkflowException("Workflow version must be positive.");activeVersion=version;definitionStatus=WorkflowDefinitionStatus.ACTIVE;}
 public void deprecate(){if(definitionStatus!=WorkflowDefinitionStatus.ACTIVE)throw new WorkflowException("Only active workflows can be deprecated.");definitionStatus=WorkflowDefinitionStatus.DEPRECATED;}
 public void archive(){definitionStatus=WorkflowDefinitionStatus.ARCHIVED;}
 static String normalize(String value){return require(value,"code").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_.-]","_");}
 static String require(String value,String field){if(value==null||value.isBlank())throw new WorkflowException(field+" is required.");return value.trim();}
 public String getCode(){return code;} public String getName(){return name;} public String getCategory(){return category;} public String getDescription(){return description;} public WorkflowDefinitionStatus getDefinitionStatus(){return definitionStatus;} public Integer getActiveVersion(){return activeVersion;}
}
