package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceRepository;
import africa.growtogether.platform.connect.ConnectSpaceType;

import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolConnectSpaceResolver {

    public static final String CONTEXT_TYPE =
            "SCHOOL_PROFILE";

    private final SchoolProfileService schoolProfiles;
    private final ConnectSpaceRepository connectSpaces;

    public SchoolConnectSpaceResolver(
            SchoolProfileService schoolProfiles,
            ConnectSpaceRepository connectSpaces
    ) {
        this.schoolProfiles =
                Objects.requireNonNull(
                        schoolProfiles,
                        "schoolProfiles must not be null"
                );

        this.connectSpaces =
                Objects.requireNonNull(
                        connectSpaces,
                        "connectSpaces must not be null"
                );
    }

    /**
     * Optionally resolves the canonical School institution space.
     *
     * This is intended for cross-capability event consumers where
     * absence of School onboarding must be a safe no-op rather than
     * an EIAM transaction failure.
     */
    @Transactional(readOnly = true)
    public Optional<ConnectSpace> findInstitutionSpace(
            UUID tenantId
    ) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Optional<SchoolProfile> profile =
                schoolProfiles.findForTenant(
                        tenantId
                );

        if (profile.isEmpty()) {
            return Optional.empty();
        }

        UUID profileId =
                profile.get().getId();

        if (profileId == null) {
            return Optional.empty();
        }

        return connectSpaces
                .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                        tenantId,
                        ConnectSpaceType.INSTITUTION,
                        CONTEXT_TYPE,
                        profileId.toString()
                );
    }


    /**
     * Resolves the canonical institution-wide GT Connect space
     * for the School Profile belonging to the supplied tenant.
     *
     * This service deliberately does NOT create spaces.
     * Creation belongs to institution onboarding/provisioning.
     */
    @Transactional(readOnly = true)
    public ConnectSpace requireInstitutionSpace(
            UUID tenantId
    ) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        SchoolProfile profile =
                schoolProfiles.getForTenant(
                        tenantId
                );

        if (profile.getId() == null) {
            throw new IllegalStateException(
                    "School profile has no persistent identifier"
            );
        }

        return connectSpaces
                .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                        tenantId,
                        ConnectSpaceType.INSTITUTION,
                        CONTEXT_TYPE,
                        profile.getId().toString()
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Canonical GT Connect institution space "
                                        + "is not provisioned for the school"
                        )
                );
    }
}
