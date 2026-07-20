package africa.growtogether.platform.eiam.user;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eiam/users")
public class UserAccountController {
    private final UserAccountService service;
    private final ApiResponses responses;

    public UserAccountController(UserAccountService service, ApiResponses responses) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('eiam.users.create')")
    public ResponseEntity<ApiResponse<UserView>> create(@Valid @RequestBody CreateUserCommand command) {
        UserView user = service.create(command);
        return ResponseEntity.created(URI.create("/api/v1/eiam/users/" + user.id()))
            .body(responses.success("GT-EIAM-USER-001", "User account created.", user));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('eiam.users.read')")
    public ApiResponse<PageView<UserView>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserAccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return responses.success("GT-EIAM-USER-007", "User accounts retrieved.",
            service.search(new UserSearchCriteria(query, status), page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('eiam.users.read')")
    public ApiResponse<UserView> get(@PathVariable UUID id) {
        return responses.success("GT-EIAM-USER-002", "User account retrieved.", service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('eiam.users.update')")
    public ApiResponse<UserView> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserCommand command) {
        return responses.success("GT-EIAM-USER-003", "User account updated.", service.update(id, command));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasAuthority('eiam.users.activate')")
    public ApiResponse<UserView> activate(@PathVariable UUID id) {
        return responses.success("GT-EIAM-USER-004", "User account activated.", service.activate(id));
    }

    @PatchMapping("/{id}/suspension")
    @PreAuthorize("hasAuthority('eiam.users.suspend')")
    public ApiResponse<UserView> suspend(@PathVariable UUID id) {
        return responses.success("GT-EIAM-USER-005", "User account suspended.", service.suspend(id));
    }

    @PatchMapping("/{id}/deactivation")
    @PreAuthorize("hasAuthority('eiam.users.deactivate')")
    public ApiResponse<UserView> deactivate(@PathVariable UUID id) {
        return responses.success("GT-EIAM-USER-006", "User account deactivated.", service.deactivate(id));
    }
}
