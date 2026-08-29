package africa.growtogether.platform.eiam.membership;

import africa.growtogether.platform.common.events.EventPublisher;
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
import africa.growtogether.platform.eiam.role.events.UserRolesChangedEvent;
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
import java.util.LinkedHashMap;
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
    private final EventPublisher eventPublisher;
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
        AuditEventService audit,
        EventPublisher eventPublisher
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
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InvitationView createInvitation(CreateInvitationCommand command) {
        UUID tenantId = activeTenant();

        String email =
            normalizeOptionalEmail(
                command.email()
            );

        String phoneNumber =
            normalizeOptionalPhone(
                command.phoneNumber()
            );

        if ((email == null) == (phoneNumber == null)) {
            throw new MembershipException(
                "Exactly one invitation contact identity is required."
            );
        }

        if (email != null) {
            invitations
                .findFirstByTenantIdAndEmailIgnoreCaseAndInvitationStatus(
                    tenantId,
                    email,
                    InvitationStatus.PENDING
                )
                .ifPresent(existing -> {
                    throw new MembershipException(
                        "A pending invitation already exists for this email address."
                    );
                });
        } else {
            invitations
                .findFirstByTenantIdAndPhoneNumberAndInvitationStatus(
                    tenantId,
                    phoneNumber,
                    InvitationStatus.PENDING
                )
                .ifPresent(existing -> {
                    throw new MembershipException(
                        "A pending invitation already exists for this phone number."
                    );
                });
        }

        Set<UUID> roleIds =
            new LinkedHashSet<>(
                command.roleIds()
            );

        roleIds.forEach(
            roleId -> requiredRole(
                roleId,
                tenantId
            )
        );

        String token =
            newToken();

        Instant expiresAt =
            command.expiresAt() == null
                ? Instant.now(clock).plus(7, ChronoUnit.DAYS)
                : command.expiresAt();

        OrganizationInvitation invitation =
            invitations.saveAndFlush(
                new OrganizationInvitation(
                    email,
                    phoneNumber,
                    hash(token),
                    expiresAt,
                    identity.userId()
                )
            );

        roleIds.forEach(
            roleId -> invitationRoles.save(
                new InvitationRole(
                    invitation.getId(),
                    roleId
                )
            )
        );

        invitationRoles.flush();

        record(
            "EIAM.INVITATION.CREATED",
            AuditOutcome.SUCCESS,
            SecuritySeverity.INFO,
            invitation.getId(),
            "Organization invitation created.",
            invitationDetails(
                invitation,
                roleIds.size()
            )
        );

        return InvitationView.from(
            invitation,
            roleIds,
            token
        );
    }

    /*
     * Internal enterprise boundary for callers that know stable
     * business role codes rather than tenant-specific EIAM role IDs.
     *
     * This does not expose a new HTTP endpoint. The existing invitation
     * lifecycle remains authoritative for token creation, role validation,
     * auditing, account reuse and membership creation.
     */
    @Transactional
    public InvitationView createInvitationByRoleCodes(
            CreateRoleCodeInvitationCommand command
    ) {

        UUID tenantId =
            activeTenant();

        Set<UUID> roleIds =
            command.roleCodes()
                .stream()
                .map(
                    roleCode ->
                        roles
                            .findByTenantIdAndCodeIgnoreCase(
                                tenantId,
                                roleCode
                            )
                            .orElseThrow(
                                RoleNotFoundException::new
                            )
                            .getId()
                )
                .collect(
                    java.util.stream.Collectors.toCollection(
                        LinkedHashSet::new
                    )
                );

        return createInvitation(
            new CreateInvitationCommand(
                command.email(),
                command.phoneNumber(),
                roleIds,
                command.expiresAt()
            )
        );
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
        record(
            "EIAM.INVITATION.RESENT",
            AuditOutcome.SUCCESS,
            SecuritySeverity.INFO,
            invitation.getId(),
            "Organization invitation resent.",
            invitationDetails(invitation)
        );
        return InvitationView.from(invitation, invitationRoleIds(tenantId, invitationId), token);
    }

    @Transactional
    public void revoke(UUID invitationId) {
        OrganizationInvitation invitation = requiredInvitation(invitationId, activeTenant());
        invitation.revoke(Instant.now(clock));
        invitations.saveAndFlush(invitation);
        record(
            "EIAM.INVITATION.REVOKED",
            AuditOutcome.SUCCESS,
            SecuritySeverity.MEDIUM,
            invitation.getId(),
            "Organization invitation revoked.",
            invitationDetails(invitation)
        );
    }

    @Transactional
    public MembershipView accept(AcceptInvitationCommand command) {

        /*
         * Backward-compatible public acceptance contract.
         *
         * Controllers and existing callers continue receiving
         * MembershipView exactly as before.
         */
        return acceptWithEvidence(
            command
        ).membership();
    }

    @Transactional
    public InvitationAcceptanceEvidence acceptWithEvidence(AcceptInvitationCommand command) {
        UUID tenantId = activeTenant();
        Instant now = Instant.now(clock);
        OrganizationInvitation invitation = invitations.findByTenantIdAndTokenHash(tenantId, hash(command.token()))
            .orElseThrow(() -> new MembershipException("Invitation token is invalid."));
        invitation.assertAcceptable(now);

        UserAccount user;

        if (invitation.isEmailTarget()) {
            user =
                users.findByTenantIdAndEmailIgnoreCase(
                    tenantId,
                    invitation.getEmail()
                )
                .map(
                    existing -> prepareExistingUser(
                        existing,
                        invitation,
                        now
                    )
                )
                .orElseGet(
                    () -> createInvitedUser(
                        tenantId,
                        invitation,
                        command,
                        now
                    )
                );
        } else {
            user =
                users.findByTenantIdAndPrimaryPhoneNumber(
                    tenantId,
                    invitation.getPhoneNumber()
                )
                .map(
                    existing -> prepareExistingUser(
                        existing,
                        invitation,
                        now
                    )
                )
                .orElseGet(
                    () -> createInvitedUser(
                        tenantId,
                        invitation,
                        command,
                        now
                    )
                );
        }
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

        publishUserRolesChanged(
            tenantId,
            user.getId(),
            now
        );

        invitation.accept(now);
        invitations.saveAndFlush(invitation);
        record("EIAM.INVITATION.ACCEPTED", AuditOutcome.SUCCESS, SecuritySeverity.INFO, invitation.getId(),
            "Organization invitation accepted.", Map.of("userId", user.getId().toString(), "membershipId", membership.getId().toString()));
        return new InvitationAcceptanceEvidence(
            invitation.getId(),
            MembershipView.from(membership)
        );
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

    private void publishUserRolesChanged(
            UUID tenantId,
            UUID userId,
            Instant occurredAt
    ) {

        eventPublisher.publish(
            new UserRolesChangedEvent(
                UUID.randomUUID(),
                tenantId,
                userId,
                occurredAt
            )
        );
    }

    private UserAccount prepareExistingUser(
            UserAccount user,
            OrganizationInvitation invitation,
            Instant now
    ) {
        if (
            user.getAccountStatus()
                == africa.growtogether.platform.eiam.user.UserAccountStatus.DEACTIVATED
        ) {
            throw new MembershipException(
                "A deactivated account cannot accept an invitation."
            );
        }

        if (
            user.getAccountStatus()
                == africa.growtogether.platform.eiam.user.UserAccountStatus.PENDING
            || user.getAccountStatus()
                == africa.growtogether.platform.eiam.user.UserAccountStatus.SUSPENDED
        ) {
            user.activate();
        }

        verifyInvitationTarget(
            user,
            invitation,
            now
        );

        return users.saveAndFlush(user);
    }

    private UserAccount createInvitedUser(
            UUID tenantId,
            OrganizationInvitation invitation,
            AcceptInvitationCommand command,
            Instant now
    ) {
        String username =
            normalize(
                command.username()
            );

        users.findByTenantIdAndUsernameIgnoreCase(
            tenantId,
            username
        )
        .ifPresent(existing -> {
            throw new DuplicateUserException(
                "username",
                "Username is already in use for this tenant."
            );
        });

        UserAccount user =
            new UserAccount(
                username,
                invitation.getEmail(),
                invitation.getPhoneNumber(),
                command.displayName(),
                passwords.hash(
                    command.password()
                )
            );

        user.activate();

        verifyInvitationTarget(
            user,
            invitation,
            now
        );

        return users.saveAndFlush(user);
    }

    private void verifyInvitationTarget(
            UserAccount user,
            OrganizationInvitation invitation,
            Instant now
    ) {
        if (invitation.isEmailTarget()) {
            user.verifyEmail(now);
            return;
        }

        user.verifyPhone(now);
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

    private static String normalize(String value) {
        return value.trim().toLowerCase();
    }

    private static String normalizeOptionalEmail(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    private static String normalizeOptionalPhone(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String phone =
            value.trim();

        if (!phone.matches("^\\+[1-9][0-9]{5,14}$")) {
            throw new MembershipException(
                "Phone number must use canonical international format."
            );
        }

        return phone;
    }

    private static Map<String, Object> invitationDetails(
            OrganizationInvitation invitation
    ) {
        Map<String, Object> details =
            new LinkedHashMap<>();

        if (invitation.isEmailTarget()) {
            details.put(
                "contactType",
                "EMAIL"
            );

            details.put(
                "email",
                invitation.getEmail()
            );
        } else {
            details.put(
                "contactType",
                "PHONE"
            );

            details.put(
                "phoneNumber",
                invitation.getPhoneNumber()
            );
        }

        return details;
    }

    private static Map<String, Object> invitationDetails(
            OrganizationInvitation invitation,
            int roleCount
    ) {
        Map<String, Object> details =
            new LinkedHashMap<>(
                invitationDetails(invitation)
            );

        details.put(
            "roleCount",
            roleCount
        );

        return details;
    }

    private static UUID activeTenant() {
        return RequestContextHolder.current().map(context -> context.tenantId())
            .filter(value -> value != null && !value.isBlank()).map(UUID::fromString)
            .orElseThrow(() -> new IllegalStateException("An active tenant is required."));
    }
}
