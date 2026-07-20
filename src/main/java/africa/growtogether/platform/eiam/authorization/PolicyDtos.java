package africa.growtogether.platform.eiam.authorization;
import jakarta.validation.constraints.*; import java.util.*;
public final class PolicyDtos { private PolicyDtos(){}
 public record UpsertPolicy(@NotBlank String code,@NotBlank String name,String description,@NotBlank String resourceType,@NotBlank String action,@NotNull PolicyEffect effect,@Min(0) int priority,String requiredPermission,String requiredRole,boolean ownerOnly,@Min(1) int minimumAal,boolean active){}
 public record PolicyView(UUID id,String code,String name,String description,String resourceType,String action,PolicyEffect effect,int priority,String requiredPermission,String requiredRole,boolean ownerOnly,int minimumAal,boolean active){static PolicyView from(AuthorizationPolicy p){return new PolicyView(p.getId(),p.getCode(),p.getName(),p.getDescription(),p.getResourceType(),p.getAction(),p.getEffect(),p.getPriority(),p.getRequiredPermission(),p.getRequiredRole(),p.isOwnerOnly(),p.getMinimumAal(),p.isActive());}}
 public record AuthorizationRequest(@NotBlank String resourceType,@NotBlank String action,String resourceId,UUID resourceOwnerId,@Min(1) int assuranceLevel){}
 public record DecisionView(AuthorizationDecision decision,String reason,List<String> matchedPolicies,Set<String> effectiveRoles,Set<String> effectivePermissions){}
}
