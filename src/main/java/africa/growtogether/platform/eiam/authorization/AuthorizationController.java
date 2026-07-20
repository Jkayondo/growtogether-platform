package africa.growtogether.platform.eiam.authorization;
import africa.growtogether.platform.common.api.*; import jakarta.validation.Valid; import java.net.URI; import java.util.*; import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import static africa.growtogether.platform.eiam.authorization.PolicyDtos.*;
@RestController @RequestMapping("/api/v1/eiam")
public class AuthorizationController {
 private final AuthorizationPolicyService policies; private final EnterpriseAuthorizationService authorization; private final ApiResponses responses;
 public AuthorizationController(AuthorizationPolicyService policies,EnterpriseAuthorizationService authorization,ApiResponses responses){this.policies=policies;this.authorization=authorization;this.responses=responses;}
 @PostMapping("/policies") @PreAuthorize("hasAuthority('eiam.policies.create')") public ResponseEntity<ApiResponse<PolicyView>> create(@Valid @RequestBody UpsertPolicy command){PolicyView p=policies.create(command);return ResponseEntity.created(URI.create("/api/v1/eiam/policies/"+p.id())).body(responses.success("GT-EIAM-POL-001","Policy created.",p));}
 @GetMapping("/policies") @PreAuthorize("hasAuthority('eiam.policies.read')") public ApiResponse<List<PolicyView>> list(){return responses.success("GT-EIAM-POL-002","Policies retrieved.",policies.list());}
 @GetMapping("/policies/{id}") @PreAuthorize("hasAuthority('eiam.policies.read')") public ApiResponse<PolicyView> get(@PathVariable UUID id){return responses.success("GT-EIAM-POL-003","Policy retrieved.",policies.get(id));}
 @PutMapping("/policies/{id}") @PreAuthorize("hasAuthority('eiam.policies.update')") public ApiResponse<PolicyView> update(@PathVariable UUID id,@Valid @RequestBody UpsertPolicy command){return responses.success("GT-EIAM-POL-004","Policy updated.",policies.update(id,command));}
 @DeleteMapping("/policies/{id}") @PreAuthorize("hasAuthority('eiam.policies.delete')") public ResponseEntity<Void> delete(@PathVariable UUID id){policies.delete(id);return ResponseEntity.noContent().build();}
 @PostMapping("/authorize") @PreAuthorize("hasAuthority('eiam.authorization.evaluate')") public ApiResponse<DecisionView> authorize(@Valid @RequestBody AuthorizationRequest request){return responses.success("GT-EIAM-AUTHZ-001","Authorization evaluated.",authorization.evaluate(request));}
}
