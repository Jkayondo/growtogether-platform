package africa.growtogether.platform.eiam.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.common.events.EventPublisher;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.security.PasswordService;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.audit.AuditEventService;
import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRole;
import africa.growtogether.platform.eiam.role.UserRoleRepository;
import africa.growtogether.platform.eiam.role.events.UserRolesChangedEvent;
import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import africa.growtogether.platform.eiam.user.UserAccountStatus;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipServiceMultiIdentityTest {

    @Mock
    OrganizationInvitationRepository invitations;

    @Mock
    InvitationRoleRepository invitationRoles;

    @Mock
    TenantMembershipRepository memberships;

    @Mock
    UserAccountRepository users;

    @Mock
    RoleRepository roles;

    @Mock
    UserRoleRepository userRoles;

    @Mock
    PasswordService passwords;

    @Mock
    EnterpriseIdentityContext identity;

    @Mock
    AuditEventService audit;

    @Mock
    EventPublisher eventPublisher;

    private MembershipService service;

    @BeforeEach
    void setUp() {
        service =
            new MembershipService(
                invitations,
                invitationRoles,
                memberships,
                users,
                roles,
                userRoles,
                passwords,
                identity,
                audit,

            eventPublisher
            );
    }

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void acceptingPhoneInvitationReusesExistingAccountAndVerifiesOnlyPhone()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String phone = "+256701234567";
        String rawToken = "phone-activation-token";

        setTenant(tenantId);

        OrganizationInvitation invitation =
            phoneInvitation(
                tenantId,
                invitationId,
                phone,
                rawToken
            );

        UserAccount existingUser =
            org.mockito.Mockito.mock(
                UserAccount.class
            );

        when(existingUser.getAccountStatus())
            .thenReturn(UserAccountStatus.ACTIVE);

        when(existingUser.getId())
            .thenReturn(userId);

        when(
            invitations.findByTenantIdAndTokenHash(
                tenantId,
                sha256(rawToken)
            )
        ).thenReturn(
            Optional.of(invitation)
        );

        when(
            users.findByTenantIdAndPrimaryPhoneNumber(
                tenantId,
                phone
            )
        ).thenReturn(
            Optional.of(existingUser)
        );

        when(
            users.saveAndFlush(existingUser)
        ).thenReturn(
            existingUser
        );

        TenantMembership membership =
            membership(
                membershipId,
                userId
            );

        when(
            memberships.findByTenantIdAndUserId(
                tenantId,
                userId
            )
        ).thenReturn(
            Optional.of(membership)
        );

        prepareRoleAssignment(
            tenantId,
            invitationId,
            userId,
            roleId
        );

        service.accept(
            new AcceptInvitationCommand(
                rawToken,
                "parent.phone",
                "Phone Parent",
                "strong-password-123"
            )
        );

        verify(existingUser)
            .verifyPhone(any(Instant.class));

        verify(existingUser, never())
            .verifyEmail(any(Instant.class));

        verify(users)
            .findByTenantIdAndPrimaryPhoneNumber(
                tenantId,
                phone
            );

        verify(users, never())
            .findByTenantIdAndEmailIgnoreCase(
                eq(tenantId),
                any()
            );

        assertThat(
            invitation.getInvitationStatus()
        ).isEqualTo(
            InvitationStatus.ACCEPTED
        );

        ArgumentCaptor<UserRolesChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(
                UserRolesChangedEvent.class
            );

        verify(
            eventPublisher
        ).publish(
            eventCaptor.capture()
        );

        UserRolesChangedEvent event =
            eventCaptor.getValue();

        assertThat(
            event.tenantId()
        ).isEqualTo(
            tenantId
        );

        assertThat(
            event.userId()
        ).isEqualTo(
            userId
        );

        assertThat(
            event.eventId()
        ).isNotNull();

        assertThat(
            event.occurredAt()
        ).isNotNull();
    }

    @Test
    void acceptingEmailInvitationReusesExistingAccountAndVerifiesOnlyEmail()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String email = "parent@example.com";
        String rawToken = "email-activation-token";

        setTenant(tenantId);

        OrganizationInvitation invitation =
            emailInvitation(
                tenantId,
                invitationId,
                email,
                rawToken
            );

        UserAccount existingUser =
            org.mockito.Mockito.mock(
                UserAccount.class
            );

        when(existingUser.getAccountStatus())
            .thenReturn(UserAccountStatus.ACTIVE);

        when(existingUser.getId())
            .thenReturn(userId);

        when(
            invitations.findByTenantIdAndTokenHash(
                tenantId,
                sha256(rawToken)
            )
        ).thenReturn(
            Optional.of(invitation)
        );

        when(
            users.findByTenantIdAndEmailIgnoreCase(
                tenantId,
                email
            )
        ).thenReturn(
            Optional.of(existingUser)
        );

        when(
            users.saveAndFlush(existingUser)
        ).thenReturn(
            existingUser
        );

        TenantMembership membership =
            membership(
                membershipId,
                userId
            );

        when(
            memberships.findByTenantIdAndUserId(
                tenantId,
                userId
            )
        ).thenReturn(
            Optional.of(membership)
        );

        prepareRoleAssignment(
            tenantId,
            invitationId,
            userId,
            roleId
        );

        service.accept(
            new AcceptInvitationCommand(
                rawToken,
                "parent.email",
                "Email Parent",
                "strong-password-123"
            )
        );

        verify(existingUser)
            .verifyEmail(any(Instant.class));

        verify(existingUser, never())
            .verifyPhone(any(Instant.class));

        verify(users)
            .findByTenantIdAndEmailIgnoreCase(
                tenantId,
                email
            );

        verify(users, never())
            .findByTenantIdAndPrimaryPhoneNumber(
                eq(tenantId),
                any()
            );

        assertThat(
            invitation.getInvitationStatus()
        ).isEqualTo(
            InvitationStatus.ACCEPTED
        );
    }

    @Test
    void acceptingPhoneInvitationCreatesVerifiedPhoneOnlyAccount()
            throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String phone = "+256702345678";
        String rawToken = "new-phone-activation-token";
        String password = "strong-password-123";

        setTenant(tenantId);

        OrganizationInvitation invitation =
            phoneInvitation(
                tenantId,
                invitationId,
                phone,
                rawToken
            );

        when(
            invitations.findByTenantIdAndTokenHash(
                tenantId,
                sha256(rawToken)
            )
        ).thenReturn(
            Optional.of(invitation)
        );

        when(
            users.findByTenantIdAndPrimaryPhoneNumber(
                tenantId,
                phone
            )
        ).thenReturn(
            Optional.empty()
        );

        when(
            users.findByTenantIdAndUsernameIgnoreCase(
                tenantId,
                "parent.new"
            )
        ).thenReturn(
            Optional.empty()
        );

        when(
            passwords.hash(password)
        ).thenReturn(
            "hashed-password"
        );

        when(
            users.saveAndFlush(
                any(UserAccount.class)
            )
        ).thenAnswer(invocation -> {
            UserAccount user =
                invocation.getArgument(0);

            setId(
                user,
                newUserId
            );

            user.setTenantId(
                tenantId
            );

            return user;
        });

        when(
            memberships.findByTenantIdAndUserId(
                tenantId,
                newUserId
            )
        ).thenReturn(
            Optional.empty()
        );

        TenantMembership membership =
            membership(
                membershipId,
                newUserId
            );

        when(
            memberships.saveAndFlush(
                any(TenantMembership.class)
            )
        ).thenReturn(
            membership
        );

        prepareRoleAssignment(
            tenantId,
            invitationId,
            newUserId,
            roleId
        );

        InvitationAcceptanceEvidence evidence =
            service.acceptWithEvidence(
            new AcceptInvitationCommand(
                rawToken,
                "Parent.New",
                "New Phone Parent",
                password
            )
        );

        assertThat(
            evidence.invitationId()
        ).isEqualTo(
            invitationId
        );

        assertThat(
            evidence.membership().id()
        ).isEqualTo(
            membershipId
        );

        assertThat(
            evidence.membership().userId()
        ).isEqualTo(
            newUserId
        );

        ArgumentCaptor<UserAccount> userCaptor =
            ArgumentCaptor.forClass(
                UserAccount.class
            );

        verify(users)
            .saveAndFlush(
                userCaptor.capture()
            );

        UserAccount created =
            userCaptor.getValue();

        assertThat(created.getId())
            .isEqualTo(newUserId);

        assertThat(created.getUsername())
            .isEqualTo("parent.new");

        assertThat(created.getEmail())
            .isNull();

        assertThat(created.getPrimaryPhoneNumber())
            .isEqualTo(phone);

        assertThat(created.getAccountStatus())
            .isEqualTo(UserAccountStatus.ACTIVE);

        assertThat(created.isPhoneVerified())
            .isTrue();

        verify(users, never())
            .findByTenantIdAndEmailIgnoreCase(
                eq(tenantId),
                any()
            );

        ArgumentCaptor<UserRole> roleCaptor =
            ArgumentCaptor.forClass(
                UserRole.class
            );

        verify(userRoles)
            .save(
                roleCaptor.capture()
            );

        assertThat(
            roleCaptor.getValue().getUserId()
        ).isEqualTo(
            newUserId
        );

        assertThat(
            roleCaptor.getValue().getRoleId()
        ).isEqualTo(
            roleId
        );

        assertThat(
            invitation.getInvitationStatus()
        ).isEqualTo(
            InvitationStatus.ACCEPTED
        );
    }

    private void prepareRoleAssignment(
            UUID tenantId,
            UUID invitationId,
            UUID userId,
            UUID roleId
    ) {

        InvitationRole invitationRole =
            new InvitationRole(
                invitationId,
                roleId
            );

        when(
            invitationRoles
                .findAllByTenantIdAndInvitationId(
                    tenantId,
                    invitationId
                )
        ).thenReturn(
            List.of(invitationRole)
        );

        when(
            roles.findByIdAndTenantId(
                roleId,
                tenantId
            )
        ).thenReturn(
            Optional.of(
                org.mockito.Mockito.mock(
                    Role.class
                )
            )
        );

        when(
            userRoles.findAllByTenantIdAndUserId(
                tenantId,
                userId
            )
        ).thenReturn(
            List.of()
        );
    }

    private static OrganizationInvitation phoneInvitation(
            UUID tenantId,
            UUID invitationId,
            String phone,
            String rawToken
    ) throws Exception {

        OrganizationInvitation invitation =
            new OrganizationInvitation(
                null,
                phone,
                sha256(rawToken),
                Instant.now().plusSeconds(3600),
                null
            );

        invitation.setTenantId(
            tenantId
        );

        setId(
            invitation,
            invitationId
        );

        return invitation;
    }

    private static OrganizationInvitation emailInvitation(
            UUID tenantId,
            UUID invitationId,
            String email,
            String rawToken
    ) throws Exception {

        OrganizationInvitation invitation =
            new OrganizationInvitation(
                email,
                sha256(rawToken),
                Instant.now().plusSeconds(3600),
                null
            );

        invitation.setTenantId(
            tenantId
        );

        setId(
            invitation,
            invitationId
        );

        return invitation;
    }

    private static TenantMembership membership(
            UUID membershipId,
            UUID userId
    ) {
        TenantMembership membership =
            org.mockito.Mockito.mock(
                TenantMembership.class
            );

        when(membership.getId())
            .thenReturn(membershipId);

        when(membership.getUserId())
            .thenReturn(userId);

        when(membership.getMembershipStatus())
            .thenReturn(MembershipStatus.ACTIVE);

        when(membership.getJoinedAt())
            .thenReturn(Instant.now());

        return membership;
    }

    private static void setTenant(
            UUID tenantId
    ) {
        RequestContextHolder.set(
            new RequestContext(
                "test",
                tenantId.toString()
            )
        );
    }

    private static void setId(
            AuditedTenantEntity entity,
            UUID id
    ) throws Exception {

        Field field =
            AuditedTenantEntity.class
                .getDeclaredField("id");

        field.setAccessible(true);
        field.set(entity, id);
    }

    private static String sha256(
            String value
    ) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                        value.getBytes(
                            StandardCharsets.UTF_8
                        )
                    )
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                exception
            );
        }
    }
}
