package africa.growtogether.platform.eiam.permission;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/eiam")
public class PermissionController {
    private final PermissionService service; private final ApiResponses responses;
    public PermissionController(PermissionService service, ApiResponses responses) { this.service = service; this.responses = responses; }

    @PostMapping("/permissions") @PreAuthorize("hasAuthority('eiam.permissions.create')")
    public ResponseEntity<ApiResponse<PermissionView>> create(@Valid @RequestBody CreatePermissionCommand command) {
        PermissionView permission = service.create(command);
        return ResponseEntity.created(URI.create("/api/v1/eiam/permissions/" + permission.id())).body(responses.success("GT-EIAM-PERM-001", "Permission created.", permission));
    }
    @GetMapping("/permissions") @PreAuthorize("hasAuthority('eiam.permissions.read')")
    public ApiResponse<List<PermissionView>> list() { return responses.success("GT-EIAM-PERM-002", "Permissions retrieved.", service.list()); }
    @GetMapping("/permissions/{id}") @PreAuthorize("hasAuthority('eiam.permissions.read')")
    public ApiResponse<PermissionView> get(@PathVariable UUID id) { return responses.success("GT-EIAM-PERM-003", "Permission retrieved.", service.get(id)); }
    @PutMapping("/permissions/{id}") @PreAuthorize("hasAuthority('eiam.permissions.update')")
    public ApiResponse<PermissionView> update(@PathVariable UUID id, @Valid @RequestBody UpdatePermissionCommand command) { return responses.success("GT-EIAM-PERM-004", "Permission updated.", service.update(id, command)); }
    @DeleteMapping("/permissions/{id}") @PreAuthorize("hasAuthority('eiam.permissions.delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @PutMapping("/roles/{roleId}/permissions") @PreAuthorize("hasAuthority('eiam.role-permissions.assign')")
    public ApiResponse<List<PermissionView>> replace(@PathVariable UUID roleId, @Valid @RequestBody ReplaceRolePermissionsCommand command) { return responses.success("GT-EIAM-PERM-005", "Role permissions replaced.", service.replaceRolePermissions(roleId, command)); }
    @GetMapping("/roles/{roleId}/permissions") @PreAuthorize("hasAuthority('eiam.role-permissions.read')")
    public ApiResponse<List<PermissionView>> rolePermissions(@PathVariable UUID roleId) { return responses.success("GT-EIAM-PERM-006", "Role permissions retrieved.", service.rolePermissions(roleId)); }
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}") @PreAuthorize("hasAuthority('eiam.role-permissions.assign')")
    public ResponseEntity<Void> remove(@PathVariable UUID roleId, @PathVariable UUID permissionId) { service.removeRolePermission(roleId, permissionId); return ResponseEntity.noContent().build(); }
}
