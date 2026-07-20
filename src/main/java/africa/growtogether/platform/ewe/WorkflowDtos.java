package africa.growtogether.platform.ewe;
import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*;
public final class WorkflowDtos {private WorkflowDtos(){}
 public record CreateDefinition(@NotBlank String code,@NotBlank String name,@NotBlank String category,String description){}
 public record CreateVersion(@NotBlank String definitionJson,boolean activate){}
 public record StartWorkflow(@NotNull UUID definitionId,Integer version,String businessKey,@NotBlank String firstStep,String executionContext,Map<String,Object> variables){}
 public record AdvanceWorkflow(@NotBlank String nextStep,String executionContext,boolean waiting){}
 public record FailWorkflow(@NotBlank String reason){}
 public record SetVariable(@NotBlank String key,String value,boolean sensitive){}
 public record RestartWorkflow(String businessKey,String executionContext,@NotBlank String firstStep){}
 public record DefinitionView(UUID id,String code,String name,String category,String description,WorkflowDefinitionStatus status,Integer activeVersion,long entityVersion){static DefinitionView from(WorkflowDefinition d){return new DefinitionView(d.getId(),d.getCode(),d.getName(),d.getCategory(),d.getDescription(),d.getDefinitionStatus(),d.getActiveVersion(),d.getVersion());}}
 public record VersionView(UUID id,UUID definitionId,int versionNumber,String checksum,boolean published,String definitionJson){static VersionView from(WorkflowVersion v){return new VersionView(v.getId(),v.getDefinitionId(),v.getVersionNumber(),v.getChecksum(),v.isPublished(),v.getDefinitionJson());}}
 public record InstanceView(UUID id,UUID definitionId,int definitionVersion,String businessKey,WorkflowInstanceStatus status,String currentStep,String executionContext,Instant startedAt,Instant completedAt,String failureReason,UUID restartOfInstanceId,int retryCount,long entityVersion){static InstanceView from(WorkflowInstance i){return new InstanceView(i.getId(),i.getDefinitionId(),i.getDefinitionVersion(),i.getBusinessKey(),i.getInstanceStatus(),i.getCurrentStep(),i.getExecutionContext(),i.getStartedAt(),i.getCompletedAt(),i.getFailureReason(),i.getRestartOfInstanceId(),i.getRetryCount(),i.getVersion());}}
 public record VariableView(UUID id,String key,String value,boolean sensitive){static VariableView from(WorkflowVariable v,boolean reveal){return new VariableView(v.getId(),v.getKey(),v.isSensitive()&&!reveal?"********":v.getValue(),v.isSensitive());}}
 public record EventView(UUID id,WorkflowEventType type,String stepCode,String message,String details,Instant occurredAt){static EventView from(WorkflowExecutionEvent e){return new EventView(e.getId(),e.getEventType(),e.getStepCode(),e.getMessage(),e.getDetails(),e.getOccurredAt());}}
}
