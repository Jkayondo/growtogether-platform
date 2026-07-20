package africa.growtogether.platform.eiam.role;

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
public class RoleController {
    private final RoleService service; private final ApiResponses responses;
    public RoleController(RoleService service, ApiResponses responses) { this.service = service; this.responses = responses; }

    @PostMapping("/roles") @PreAuthorize("hasAuthority('eiam.roles.create')")
    public ResponseEntity<ApiResponse<RoleView>> create(@Valid @RequestBody CreateRoleCommand command) {
        RoleView role = service.create(command);
        return ResponseEntity.created(URI.create("/api/v1/eiam/roles/" + role.id())).body(responses.success("GT-EIAM-ROLE-001", "Role created.", role));
    }
    @GetMapping("/roles") @PreAuthorize("hasAuthority('eiam.roles.read')")
    public ApiResponse<List<RoleView>> list() { return responses.success("GT-EIAM-ROLE-002", "Roles retrieved.", service.list()); }
    @GetMapping("/roles/{id}") @PreAuthorize("hasAuthority('eiam.roles.read')")
    public ApiResponse<RoleView> get(@PathVariable UUID id) { return responses.success("GT-EIAM-ROLE-003", "Role retrieved.", service.get(id)); }
    @PutMapping("/roles/{id}") @PreAuthorize("hasAuthority('eiam.roles.update')")
    public ApiResponse<RoleView> update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleCommand command) { return responses.success("GT-EIAM-ROLE-004", "Role updated.", service.update(id, command)); }
    @DeleteMapping("/roles/{id}") @PreAuthorize("hasAuthority('eiam.roles.delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @PutMapping("/users/{userId}/roles") @PreAuthorize("hasAuthority('eiam.user-roles.assign')")
    public ApiResponse<List<RoleView>> replace(@PathVariable UUID userId, @Valid @RequestBody ReplaceUserRolesCommand command) { return responses.success("GT-EIAM-ROLE-005", "User roles replaced.", service.replaceUserRoles(userId, command)); }
    @GetMapping("/users/{userId}/roles") @PreAuthorize("hasAuthority('eiam.user-roles.read')")
    public ApiResponse<List<RoleView>> userRoles(@PathVariable UUID userId) { return responses.success("GT-EIAM-ROLE-006", "User roles retrieved.", service.userRoles(userId)); }
    @DeleteMapping("/users/{userId}/roles/{roleId}") @PreAuthorize("hasAuthority('eiam.user-roles.assign')")
    public ResponseEntity<Void> remove(@PathVariable UUID userId, @PathVariable UUID roleId) { service.removeUserRole(userId, roleId); return ResponseEntity.noContent().build(); }
}
