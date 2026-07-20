package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eiam")
public class MembershipController {
    private final MembershipService service;
    private final ApiResponses responses;

    public MembershipController(MembershipService service, ApiResponses responses) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping("/invitations")
    @PreAuthorize("hasAuthority('eiam.invitations.create')")
    public ResponseEntity<ApiResponse<InvitationView>> create(@Valid @RequestBody CreateInvitationCommand command) {
        InvitationView invitation = service.createInvitation(command);
        return ResponseEntity.created(URI.create("/api/v1/eiam/invitations/" + invitation.id()))
            .body(responses.success("GT-EIAM-INV-001", "Invitation created.", invitation));
    }

    @GetMapping("/invitations")
    @PreAuthorize("hasAuthority('eiam.invitations.read')")
    public ApiResponse<List<InvitationView>> invitations() {
        return responses.success("GT-EIAM-INV-002", "Invitations retrieved.", service.listInvitations());
    }

    @PostMapping("/invitations/{id}/resend")
    @PreAuthorize("hasAuthority('eiam.invitations.resend')")
    public ApiResponse<InvitationView> resend(@PathVariable UUID id) {
        return responses.success("GT-EIAM-INV-003", "Invitation resent.", service.resend(id));
    }

    @DeleteMapping("/invitations/{id}")
    @PreAuthorize("hasAuthority('eiam.invitations.revoke')")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        service.revoke(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/accept")
    public ApiResponse<MembershipView> accept(@Valid @RequestBody AcceptInvitationCommand command) {
        return responses.success("GT-EIAM-INV-004", "Invitation accepted.", service.accept(command));
    }

    @GetMapping("/memberships")
    @PreAuthorize("hasAuthority('eiam.memberships.read')")
    public ApiResponse<List<MembershipView>> memberships() {
        return responses.success("GT-EIAM-MEM-001", "Memberships retrieved.", service.listMemberships());
    }

    @PatchMapping("/memberships/{id}/status/{status}")
    @PreAuthorize("hasAuthority('eiam.memberships.update')")
    public ApiResponse<MembershipView> status(@PathVariable UUID id, @PathVariable MembershipStatus status) {
        return responses.success("GT-EIAM-MEM-002", "Membership status updated.", service.changeMembershipStatus(id, status));
    }
}
