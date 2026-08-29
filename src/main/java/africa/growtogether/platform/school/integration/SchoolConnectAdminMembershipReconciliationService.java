package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;

import africa.growtogether.platform.connect.ConnectMemberRole;
import africa.growtogether.platform.connect.ConnectMembershipStatus;
import africa.growtogether.platform.connect.ConnectRoleAuthoritySources;
import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceMember;
import africa.growtogether.platform.connect.ConnectSpaceMemberRepository;

import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleRepository;
import africa.growtogether.platform.eiam.role.UserRoleRepository;

import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import africa.growtogether.platform.eiam.user.UserAccountStatus;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolConnectAdminMembershipReconciliationService {

    private static final String SCHOOL_ADMIN_ROLE_CODE =
            "SCHOOL_ADMIN";

    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final UserAccountRepository users;
    private final ConnectSpaceMemberRepository members;


    public SchoolConnectAdminMembershipReconciliationService(
            RoleRepository roles,
            UserRoleRepository userRoles,
            UserAccountRepository users,
            ConnectSpaceMemberRepository members
    ) {
        this.roles =
                Objects.requireNonNull(
                        roles,
                        "roles must not be null"
                );

        this.userRoles =
                Objects.requireNonNull(
                        userRoles,
                        "userRoles must not be null"
                );

        this.users =
                Objects.requireNonNull(
                        users,
                        "users must not be null"
                );

        this.members =
                Objects.requireNonNull(
                        members,
                        "members must not be null"
                );
    }


    /**
     * Reconciles one user's canonical School institution membership
     * against the authoritative EIAM SCHOOL_ADMIN state.
     */
    @Transactional
    public void reconcile(
            UUID tenantId,
            UUID userId,
            ConnectSpace institutionSpace
    ) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                userId,
                "userId must not be null"
        );

        Objects.requireNonNull(
                institutionSpace,
                "institutionSpace must not be null"
        );

        if (
                institutionSpace.getTenantId() == null
                        || !tenantId.equals(
                                institutionSpace.getTenantId()
                        )
        ) {
            throw new TenantScopeViolationException(
                    "GT Connect institution space tenant does not "
                            + "match School administrator reconciliation tenant."
            );
        }

        UUID spaceId =
                institutionSpace.getId();

        if (spaceId == null) {
            throw new IllegalStateException(
                    "GT Connect institution space must be persisted "
                            + "before administrator reconciliation."
            );
        }

        boolean eligible =
                isActiveSchoolAdministrator(
                        tenantId,
                        userId
                );

        ConnectSpaceMember activeMembership =
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                spaceId,
                                userId,
                                ConnectMembershipStatus.ACTIVE
                        )
                        .orElse(null);

        if (eligible) {

            if (activeMembership == null) {

                ConnectSpaceMember created =
                        new ConnectSpaceMember(
                                tenantId,
                                spaceId,
                                userId,
                                ConnectMemberRole.ADMIN
                        );

                created.applyAuthoritativeRole(
                        ConnectMemberRole.ADMIN,
                        ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
                );

                members.save(
                        created
                );

                return;
            }

            activeMembership.applyAuthoritativeRole(
                    ConnectMemberRole.ADMIN,
                    ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
            );

            members.save(
                    activeMembership
            );

            return;
        }

        if (
                activeMembership != null
                        && ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
                        .equals(
                                activeMembership.getRoleAuthoritySource()
                        )
        ) {

            activeMembership.releaseAuthoritativeRole(
                    ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
            );

            members.save(
                    activeMembership
            );
        }
    }


    private boolean isActiveSchoolAdministrator(
            UUID tenantId,
            UUID userId
    ) {

        UserAccount user =
                users.findByIdAndTenantId(
                                userId,
                                tenantId
                        )
                        .orElse(null);

        if (
                user == null
                        || user.getAccountStatus()
                        != UserAccountStatus.ACTIVE
        ) {
            return false;
        }

        Role schoolAdminRole =
                roles.findByTenantIdAndCodeIgnoreCase(
                                tenantId,
                                SCHOOL_ADMIN_ROLE_CODE
                        )
                        .orElse(null);

        if (schoolAdminRole == null) {
            return false;
        }

        return userRoles
                .findAllByTenantIdAndUserId(
                        tenantId,
                        userId
                )
                .stream()
                .anyMatch(
                        assignment ->
                                schoolAdminRole.getId().equals(
                                        assignment.getRoleId()
                                )
                );
    }
}
