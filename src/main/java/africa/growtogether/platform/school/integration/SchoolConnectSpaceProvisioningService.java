package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;

import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceRepository;
import africa.growtogether.platform.connect.ConnectSpaceType;

import africa.growtogether.platform.school.profile.SchoolProfile;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolConnectSpaceProvisioningService {

    private final ConnectSpaceRepository connectSpaces;

    private final SchoolConnectAdminMembershipProvisioningService
            adminMemberships;


    public SchoolConnectSpaceProvisioningService(
            ConnectSpaceRepository connectSpaces,
            SchoolConnectAdminMembershipProvisioningService adminMemberships
    ) {

        this.connectSpaces =
                Objects.requireNonNull(
                        connectSpaces,
                        "connectSpaces must not be null"
                );

        this.adminMemberships =
                Objects.requireNonNull(
                        adminMemberships,
                        "adminMemberships must not be null"
                );
    }

    /**
     * Ensures that a persisted School Profile has exactly one
     * canonical GT Connect INSTITUTION space.
     *
     * This is an internal provisioning operation. It deliberately
     * does not require an interactive Connect administrator.
     */
    @Transactional
    public ConnectSpace provisionInstitutionSpace(
            UUID tenantId,
            SchoolProfile profile
    ) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                profile,
                "profile must not be null"
        );

        if (
                profile.getTenantId() == null
                        || !tenantId.equals(
                                profile.getTenantId()
                        )
        ) {
            throw new TenantScopeViolationException(
                    "School Profile tenant does not match "
                            + "the Connect provisioning tenant."
            );
        }

        UUID profileId =
                profile.getId();

        if (profileId == null) {
            throw new IllegalStateException(
                    "School Profile must be persisted before "
                            + "GT Connect provisioning."
            );
        }

        String contextReference =
                profileId.toString();

        ConnectSpace institutionSpace =
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                SchoolConnectSpaceResolver.CONTEXT_TYPE,
                                contextReference
                        )
                        .orElseGet(
                                () -> connectSpaces.saveAndFlush(
                                        new ConnectSpace(
                                                tenantId,
                                                ConnectSpaceType.INSTITUTION,
                                                profile.getSchoolName(),
                                                SchoolConnectSpaceResolver.CONTEXT_TYPE,
                                                contextReference
                                        )
                                )
                        );

        adminMemberships.ensureSchoolAdministrators(
                tenantId,
                institutionSpace
        );

        return institutionSpace;
    }
}
