package africa.growtogether.platform.ewe;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="ewe_workflow_variables",uniqueConstraints=@UniqueConstraint(name="uk_ewe_variable_instance_key",columnNames={"tenant_id","instance_id","variable_key"}))
public class WorkflowVariable extends AuditedTenantEntity {
 @Column(name="instance_id",nullable=false,updatable=false) private UUID instanceId;
 @Column(name="variable_key",nullable=false,length=160,updatable=false) private String key;
 @Column(name="variable_value",columnDefinition="jsonb") private String value;
 @Column(name="sensitive",nullable=false) private boolean sensitive;
 protected WorkflowVariable(){}
 public WorkflowVariable(UUID instanceId,String key,String value,boolean sensitive){this.instanceId=instanceId;this.key=WorkflowDefinition.normalize(key);this.value=value;this.sensitive=sensitive;}
 public void update(String value,boolean sensitive){this.value=value;this.sensitive=sensitive;}
 public UUID getInstanceId(){return instanceId;} public String getKey(){return key;} public String getValue(){return value;} public boolean isSensitive(){return sensitive;}
}
