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
import africa.growtogether.platform.eiam.role.UserRole;
import africa.growtogether.platform.eiam.role.UserRoleRepository;

import africa.growtogether.platform.eiam.user.UserAccount;
import africa.growtogether.platform.eiam.user.UserAccountRepository;
import africa.growtogether.platform.eiam.user.UserAccountStatus;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolConnectAdminMembershipProvisioningService {

    private static final String SCHOOL_ADMIN_ROLE_CODE =
            "SCHOOL_ADMIN";

    private final RoleRepository roles;

    private final UserRoleRepository userRoles;

    private final UserAccountRepository users;

    private final ConnectSpaceMemberRepository members;


    public SchoolConnectAdminMembershipProvisioningService(
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
     * Ensures that every ACTIVE EIAM user holding SCHOOL_ADMIN
     * for this tenant is an ACTIVE ADMIN member of the canonical
     * School institution GT Connect space.
     *
     * Existing ACTIVE memberships are reconciled to authoritative
     * ADMIN while preserving their previous ordinary Connect role
     * for restoration when SCHOOL_ADMIN authority is withdrawn.
     *
     * Historical LEFT/REMOVED memberships do not block creation
     * of a new ACTIVE membership.
     */
    @Transactional
    public int ensureSchoolAdministrators(
            UUID tenantId,
            ConnectSpace institutionSpace
    ) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
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
                            + "match School administrator provisioning tenant."
            );
        }

        UUID spaceId =
                institutionSpace.getId();

        if (spaceId == null) {
            throw new IllegalStateException(
                    "GT Connect institution space must be persisted "
                            + "before administrator membership provisioning."
            );
        }

        Role schoolAdminRole =
                roles.findByTenantIdAndCodeIgnoreCase(
                        tenantId,
                        SCHOOL_ADMIN_ROLE_CODE
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "SCHOOL_ADMIN role is not provisioned "
                                        + "for tenant."
                        )
                );

        int created = 0;

        for (
                UserRole assignment :
                userRoles.findAllByTenantIdAndRoleId(
                        tenantId,
                        schoolAdminRole.getId()
                )
        ) {

            UserAccount user =
                    users.findByIdAndTenantId(
                            assignment.getUserId(),
                            tenantId
                    )
                    .orElse(null);

            if (
                    user == null
                            || user.getAccountStatus()
                            != UserAccountStatus.ACTIVE
            ) {
                continue;
            }

            ConnectSpaceMember activeMembership =
                    members
                            .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                    tenantId,
                                    spaceId,
                                    user.getId(),
                                    ConnectMembershipStatus.ACTIVE
                            )
                            .orElse(null);

            if (activeMembership != null) {

                activeMembership.applyAuthoritativeRole(
                        ConnectMemberRole.ADMIN,
                        ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
                );

                members.save(
                        activeMembership
                );

                continue;
            }

            ConnectSpaceMember createdMembership =
                    new ConnectSpaceMember(
                            tenantId,
                            spaceId,
                            user.getId(),
                            ConnectMemberRole.ADMIN
                    );

            createdMembership.applyAuthoritativeRole(
                    ConnectMemberRole.ADMIN,
                    ConnectRoleAuthoritySources.EIAM_SCHOOL_ADMIN
            );

            members.save(
                    createdMembership
            );

            created++;
        }

        return created;
    }
}
