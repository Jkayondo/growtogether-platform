package africa.growtogether.platform.eiam.tenant;
import africa.growtogether.platform.common.api.*;
import jakarta.validation.Valid; import java.net.URI; import java.util.UUID;
import org.springframework.http.ResponseEntity; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/eiam/tenants")
public class TenantController {
 private final TenantProvisioningService service; private final ApiResponses responses;
 public TenantController(TenantProvisioningService service,ApiResponses responses){this.service=service;this.responses=responses;}
 @PostMapping("/provision") @PreAuthorize("hasAuthority('platform.tenants.provision')")
 public ResponseEntity<ApiResponse<TenantView>> provision(@Valid @RequestBody ProvisionTenantCommand command){TenantView view=service.provision(command);return ResponseEntity.created(URI.create("/api/v1/eiam/tenants/"+view.tenantId())).body(responses.success("GT-EIAM-TENANT-001","Tenant provisioned.",view));}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('platform.tenants.read')") public ApiResponse<Tenant> get(@PathVariable UUID id){return responses.success("GT-EIAM-TENANT-002","Tenant retrieved.",service.get(id));}
 @PatchMapping("/{id}/status/{status}") @PreAuthorize("hasAuthority('platform.tenants.manage')") public ApiResponse<Tenant> status(@PathVariable UUID id,@PathVariable TenantStatus status){return responses.success("GT-EIAM-TENANT-003","Tenant status updated.",service.changeStatus(id,status));}
}
