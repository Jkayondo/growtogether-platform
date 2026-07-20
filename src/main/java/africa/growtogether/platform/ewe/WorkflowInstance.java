package africa.growtogether.platform.ewe;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="ewe_workflow_instances",indexes={@Index(name="ix_ewe_instance_tenant_status",columnList="tenant_id,instance_status"),@Index(name="ix_ewe_instance_business_key",columnList="tenant_id,business_key")})
public class WorkflowInstance extends AuditedTenantEntity {
 @Column(name="definition_id",nullable=false,updatable=false) private UUID definitionId;
 @Column(name="definition_version",nullable=false,updatable=false) private int definitionVersion;
 @Column(name="business_key",length=180) private String businessKey;
 @Enumerated(EnumType.STRING) @Column(name="instance_status",nullable=false,length=20) private WorkflowInstanceStatus instanceStatus=WorkflowInstanceStatus.CREATED;
 @Column(name="current_step",length=160) private String currentStep;
 @Column(name="execution_context",nullable=false,columnDefinition="jsonb") private String executionContext="{}";
 @Column(name="started_at") private Instant startedAt;
 @Column(name="completed_at") private Instant completedAt;
 @Column(name="failure_reason",length=1000) private String failureReason;
 @Column(name="restart_of_instance_id") private UUID restartOfInstanceId;
 @Column(name="retry_count",nullable=false) private int retryCount;
 protected WorkflowInstance(){}
 public WorkflowInstance(UUID definitionId,int definitionVersion,String businessKey,String context,UUID restartOf){this.definitionId=definitionId;this.definitionVersion=definitionVersion;this.businessKey=businessKey;this.executionContext=(context==null||context.isBlank())?"{}":context;this.restartOfInstanceId=restartOf;}
 public void start(String firstStep){requireState(WorkflowInstanceStatus.CREATED);instanceStatus=WorkflowInstanceStatus.RUNNING;currentStep=firstStep;startedAt=Instant.now();}
 public void advance(String nextStep,String context){requireRunnable();currentStep=WorkflowDefinition.require(nextStep,"nextStep");if(context!=null&&!context.isBlank())executionContext=context;instanceStatus=WorkflowInstanceStatus.RUNNING;failureReason=null;}
 public void waitAt(String step){requireRunnable();currentStep=WorkflowDefinition.require(step,"step");instanceStatus=WorkflowInstanceStatus.WAITING;}
 public void resume(){requireState(WorkflowInstanceStatus.WAITING);instanceStatus=WorkflowInstanceStatus.RUNNING;}
 public void complete(){requireRunnable();instanceStatus=WorkflowInstanceStatus.COMPLETED;completedAt=Instant.now();currentStep=null;failureReason=null;}
 public void fail(String reason){requireRunnable();instanceStatus=WorkflowInstanceStatus.FAILED;failureReason=WorkflowDefinition.require(reason,"reason");}
 public void retry(){requireState(WorkflowInstanceStatus.FAILED);retryCount++;failureReason=null;instanceStatus=WorkflowInstanceStatus.RUNNING;}
 public void cancel(){if(instanceStatus==WorkflowInstanceStatus.COMPLETED||instanceStatus==WorkflowInstanceStatus.CANCELLED)throw new WorkflowException("Terminal workflow instances cannot be cancelled.");instanceStatus=WorkflowInstanceStatus.CANCELLED;completedAt=Instant.now();}
 private void requireRunnable(){if(instanceStatus!=WorkflowInstanceStatus.RUNNING&&instanceStatus!=WorkflowInstanceStatus.WAITING)throw new WorkflowException("Workflow instance is not runnable from state "+instanceStatus+".");}
 private void requireState(WorkflowInstanceStatus expected){if(instanceStatus!=expected)throw new WorkflowException("Expected workflow state "+expected+" but was "+instanceStatus+".");}
 public UUID getDefinitionId(){return definitionId;} public int getDefinitionVersion(){return definitionVersion;} public String getBusinessKey(){return businessKey;} public WorkflowInstanceStatus getInstanceStatus(){return instanceStatus;} public String getCurrentStep(){return currentStep;} public String getExecutionContext(){return executionContext;} public Instant getStartedAt(){return startedAt;} public Instant getCompletedAt(){return completedAt;} public String getFailureReason(){return failureReason;} public UUID getRestartOfInstanceId(){return restartOfInstanceId;} public int getRetryCount(){return retryCount;}
}
