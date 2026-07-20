package africa.growtogether.platform.ecs;
import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*;
public final class ConfigurationDtos {private ConfigurationDtos(){}
 public record UpsertDefinition(@NotBlank String code,@NotBlank String name,@NotBlank String category,String description,@NotNull ConfigurationDataType dataType,String defaultValue,String validationRules,@NotEmpty Set<ConfigurationScope> allowedScopes,boolean required,boolean secret,Boolean active){}
 public record DefinitionView(UUID id,String code,String name,String category,String description,ConfigurationDataType dataType,String defaultValue,String validationRules,Set<ConfigurationScope> allowedScopes,boolean required,boolean secret,boolean active,long version){static DefinitionView from(ConfigurationDefinition d){return new DefinitionView(d.getId(),d.getCode(),d.getName(),d.getCategory(),d.getDescription(),d.getDataType(),d.isSecret()?null:d.getDefaultValue(),d.getValidationRules(),d.getAllowedScopes(),d.isRequired(),d.isSecret(),d.isActive(),d.getVersion());}}
 public record PutValue(@NotNull ConfigurationScope scope,String countryCode,UUID organizationId,UUID tenantId,@NotNull String value,String reason){}
 public record ResolveRequest(@NotBlank String code,String countryCode,UUID organizationId,UUID tenantId){}
 public record ResolvedValue(String code,ConfigurationDataType dataType,String value,ConfigurationScope sourceScope,long definitionVersion,boolean secret){}
 public record VersionView(UUID id,long versionNumber,String valueHash,String changeReason,String changedBy,String correlationId,Long rollbackFromVersion,Instant createdAt,boolean secret){static VersionView from(ConfigurationVersion v,boolean secret){return new VersionView(v.getId(),v.getVersionNumber(),v.getValueHash(),v.getChangeReason(),v.getChangedBy(),v.getCorrelationId(),v.getRollbackFromVersion(),v.getCreatedAt(),secret);}}
 public record RollbackRequest(@Positive long versionNumber,@Size(max=500) String reason){}
}
