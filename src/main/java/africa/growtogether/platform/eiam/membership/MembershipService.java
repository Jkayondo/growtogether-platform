package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.audit.AuditEventCategory;
import africa.growtogether.platform.eiam.audit.AuditEventService;
import africa.growtogether.platform.eiam.audit.AuditOutcome;
import africa.growtogether.platform.eiam.audit.RecordAuditEventCommand;
import africa.growtogether.platform.eiam.audit.SecuritySeverity;
import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleNotFoundException;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRole;
import africa.growtogether.platform.eiam.role.UserRoleRepository;
import africa.growtogether.platform.eiam.user.DuplicateUserException;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipService {
    private final OrganizationInvitationRepository invitations;
    private final InvitationRoleRepository invitationRoles;
    private final TenantMembershipRepository memberships;
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordService passwords;
    private final EnterpriseIdentityContext identity;
    private final AuditEventService audit;
    private final SecureRandom random = new SecureRandom();
    private final Clock clock = Clock.systemUTC();

    public MembershipService(
        OrganizationInvitationRepository invitations,
        InvitationRoleRepository invitationRoles,
        TenantMembershipRepository memberships,
        UserAccountRepository users,
        RoleRepository roles,
        UserRoleRepository userRoles,
        PasswordService passwords,
        EnterpriseIdentityContext identity,
        AuditEventService audit
    ) {
        this.invitations = invitations;
        this.invitationRoles = invitationRoles;
        this.memberships = memberships;
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.passwords = passwords;
        this.identity = identity;
        this.audit = audit;
    }

    @Transactional
    public InvitationView createInvitation(CreateInvitationCommand command) {
        UUID tenantId = activeTenant();
        String email = normalize(command.email());
        invitations.findFirstByTenantIdAndEmailIgnoreCaseAndInvitationStatus(tenantId, email, InvitationStatus.PENDING)
            .ifPresent(existing -> { throw new MembershipException("A pending invitation already exists for this email address."); });

        Set<UUID> roleIds = new LinkedHashSet<>(command.roleIds());
        roleIds.forEach(roleId -> requiredRole(roleId, tenantId));
        String token = newToken();
        Instant expiresAt = command.expiresAt() == null ? Instant.now(clock).plus(7, ChronoUnit.DAYS) : command.expiresAt();
        OrganizationInvitation invitation = invitations.saveAndFlush(
            new OrganizationInvitation(email, hash(token), expiresAt, identity.userId()));
        roleIds.forEach(roleId -> invitationRoles.save(new InvitationRole(invitation.getId(), roleId)));
        invitationRoles.flush();
        record("EIAM.INVITATION.CREATED", AuditOutcome.SUCCESS, SecuritySeverity.INFO, invitation.getId(),
            "Organization invitation created.", Map.of("email", email, "roleCount", roleIds.size()));
        return InvitationView.from(invitation, roleIds, token);
    }

    @Transactional(readOnly = true)
    public List<InvitationView> listInvitations() {
        UUID tenantId = activeTenant();
        return invitations.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
            .map(invitation -> InvitationView.from(invitation, invitationRoleIds(tenantId, invitation.getId()), null))
            .toList();
    }

    @Transactional
    public InvitationView resend(UUID invitationId) {
        UUID tenantId = activeTenant();
        OrganizationInvitation invitation = requiredInvitation(invitationId, tenantId);
        String token = newToken();
        invitation.replaceToken(hash(token), Instant.now(clock).plus(7, ChronoUnit.DAYS));
        invitations.saveAndFlush(invitation);
        record("EIAM.INVITATION.RESENT", AuditOutcome.SUCCESS, SecuritySeverity.INFO, invitation.getId(),
            "Organization invitation resent.", Map.of("email", invitation.getEmail()));
        return InvitationView.from(invitation, invitationRoleIds(tenantId, invitationId), token);
    }

    @Transactional
    public void revoke(UUID invitationId) {
        OrganizationInvitation invitation = requiredInvitation(invitationId, activeTenant());
        invitation.revoke(Instant.now(clock));
        invitations.saveAndFlush(invitation);
        record("EIAM.INVITATION.REVOKED", AuditOutcome.SUCCESS, SecuritySeverity.MEDIUM, invitation.getId(),
            "Organization invitation revoked.", Map.of("email", invitation.getEmail()));
    }

    @Transactional
    public MembershipView accept(AcceptInvitationCommand command) {
        UUID tenantId = activeTenant();
        Instant now = Instant.now(clock);
        OrganizationInvitation invitation = invitations.findByTenantIdAndTokenHash(tenantId, hash(command.token()))
            .orElseThrow(() -> new MembershipException("Invitation token is invalid."));
        invitation.assertAcceptable(now);

        UserAccount user = users.findByTenantIdAndEmailIgnoreCase(tenantId, invitation.getEmail())
            .map(existing -> prepareExistingUser(existing, now))
            .orElseGet(() -> createInvitedUser(tenantId, invitation, command));
        TenantMembership membership = memberships.findByTenantIdAndUserId(tenantId, user.getId())
            .orElseGet(() -> memberships.saveAndFlush(new TenantMembership(user.getId(), now)));
        if (membership.getMembershipStatus() == MembershipStatus.REMOVED) {
            throw new MembershipException("This user was previously removed from the tenant and requires administrator review.");
        }

        Set<UUID> roleIds = invitationRoleIds(tenantId, invitation.getId());
        for (UUID roleId : roleIds) {
            requiredRole(roleId, tenantId);
            boolean assigned = userRoles.findAllByTenantIdAndUserId(tenantId, user.getId()).stream()
                .anyMatch(existing -> existing.getRoleId().equals(roleId));
            if (!assigned) userRoles.save(new UserRole(user.getId(), roleId));
        }
        userRoles.flush();
        invitation.accept(now);
        invitations.saveAndFlush(invitation);
        record("EIAM.INVITATION.ACCEPTED", AuditOutcome.SUCCESS, SecuritySeverity.INFO, invitation.getId(),
            "Organization invitation accepted.", Map.of("userId", user.getId().toString(), "membershipId", membership.getId().toString()));
        return MembershipView.from(membership);
    }

    @Transactional(readOnly = true)
    public List<MembershipView> listMemberships() {
        return memberships.findAllByTenantIdOrderByCreatedAtDesc(activeTenant()).stream().map(MembershipView::from).toList();
    }

    @Transactional
    public MembershipView changeMembershipStatus(UUID membershipId, MembershipStatus target) {
        TenantMembership membership = requiredMembership(membershipId, activeTenant());
        membership.changeStatus(target, Instant.now(clock));
        memberships.saveAndFlush(membership);
        record("EIAM.MEMBERSHIP.STATUS_CHANGED", AuditOutcome.SUCCESS, SecuritySeverity.MEDIUM, membership.getId(),
            "Tenant membership status changed.", Map.of("status", target.name(), "userId", membership.getUserId().toString()));
        return MembershipView.from(membership);
    }

    private UserAccount prepareExistingUser(UserAccount user, Instant now) {
        if (user.getAccountStatus() == africa.growtogether.platform.eiam.user.UserAccountStatus.DEACTIVATED) {
            throw new MembershipException("A deactivated account cannot accept an invitation.");
        }
        if (user.getAccountStatus() == africa.growtogether.platform.eiam.user.UserAccountStatus.PENDING
            || user.getAccountStatus() == africa.growtogether.platform.eiam.user.UserAccountStatus.SUSPENDED) {
            user.activate();
        }
        user.verifyEmail(now);
        return users.saveAndFlush(user);
    }

    private UserAccount createInvitedUser(UUID tenantId, OrganizationInvitation invitation, AcceptInvitationCommand command) {
        String username = normalize(command.username());
        users.findByTenantIdAndUsernameIgnoreCase(tenantId, username)
            .ifPresent(existing -> { throw new DuplicateUserException("username", "Username is already in use for this tenant."); });
        UserAccount user = new UserAccount(username, invitation.getEmail(), command.displayName(), passwords.hash(command.password()));
        user.activate();
        user.verifyEmail(Instant.now(clock));
        return users.saveAndFlush(user);
    }

    private OrganizationInvitation requiredInvitation(UUID id, UUID tenantId) {
        return invitations.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new MembershipException("Invitation was not found."));
    }

    private TenantMembership requiredMembership(UUID id, UUID tenantId) {
        return memberships.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new MembershipException("Membership was not found."));
    }

    private Role requiredRole(UUID id, UUID tenantId) {
        return roles.findByIdAndTenantId(id, tenantId).orElseThrow(RoleNotFoundException::new);
    }

    private Set<UUID> invitationRoleIds(UUID tenantId, UUID invitationId) {
        return invitationRoles.findAllByTenantIdAndInvitationId(tenantId, invitationId).stream()
            .map(InvitationRole::getRoleId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private void record(String eventType, AuditOutcome outcome, SecuritySeverity severity, UUID resourceId,
        String message, Map<String, Object> details) {
        audit.record(new RecordAuditEventCommand(eventType, AuditEventCategory.IDENTITY, outcome, severity,
            "TENANT_MEMBERSHIP", resourceId.toString(), message, details));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private static String normalize(String value) { return value.trim().toLowerCase(); }
    private static UUID activeTenant() {
        return RequestContextHolder.current().map(context -> context.tenantId())
            .filter(value -> value != null && !value.isBlank()).map(UUID::fromString)
            .orElseThrow(() -> new IllegalStateException("An active tenant is required."));
    }
}
