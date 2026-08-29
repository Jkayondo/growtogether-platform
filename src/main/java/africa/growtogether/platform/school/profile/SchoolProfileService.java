package africa.growtogether.platform.school.profile;

import africa.growtogether.platform.school.integration.SchoolConnectSpaceProvisioningService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class SchoolProfileService {

    private final SchoolProfileRepository repository;

    private final SchoolConnectSpaceProvisioningService
            connectProvisioning;

    public SchoolProfileService(
            SchoolProfileRepository repository,
            SchoolConnectSpaceProvisioningService connectProvisioning
    ) {
        this.repository = repository;
        this.connectProvisioning = connectProvisioning;
    }

    @Transactional
    public SchoolProfile create(
            UUID tenantId,
            CreateSchoolProfileCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        /*
         * V032 already guarantees one school profile per tenant.
         * Application-level protection gives a clearer error before
         * PostgreSQL has to reject the duplicate.
         */
        if (
                repository.existsByTenantId(
                        tenantId
                )
        ) {
            throw new IllegalArgumentException(
                    "School profile already exists for tenant"
            );
        }

        String timezone =
                validateTimezone(
                        command.timezone()
                );

        SchoolProfile schoolProfile =
                new SchoolProfile(
                        command.schoolCode(),
                        command.schoolName(),
                        command.legalName(),
                        command.educationSystem(),
                        command.countryCode(),
                        command.defaultCurrency(),
                        timezone,
                        command.email(),
                        command.phoneNumber(),
                        command.website()
                );

        /*
         * IMPROVEMENT:
         * School Profile is explicitly tenant-owned just like the
         * other GT School aggregate roots.
         */
        schoolProfile.setTenantId(
                tenantId
        );

        SchoolProfile saved =
                repository.saveAndFlush(
                        schoolProfile
                );

        /*
         * The School Profile and its canonical GT Connect
         * institution space form one onboarding transaction.
         *
         * If Connect provisioning fails, profile creation rolls back.
         */
        connectProvisioning.provisionInstitutionSpace(
                tenantId,
                saved
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public SchoolProfile get(
            UUID tenantId,
            UUID id
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "School profile not found for tenant"
                        )
                );
    }

    /**
     * Optional tenant lookup for integrations that must tolerate
     * tenants where GT School has not yet been onboarded.
     */
    @Transactional(readOnly = true)
    public Optional<SchoolProfile> findForTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        return repository.findByTenantId(
                tenantId
        );
    }

    /*
     * Preferred lookup for shared school-level configuration such
     * as the timetable timezone. V032 permits one profile per tenant.
     */
    @Transactional(readOnly = true)
    public SchoolProfile getForTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        return repository
                .findByTenantId(
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "School profile not found for tenant"
                        )
                );
    }

    /*
     * Availability/timetable calculations require an actual timezone.
     * This method deliberately fails instead of silently assuming UTC
     * or Africa/Kampala.
     */
    @Transactional(readOnly = true)
    public ZoneId requireTimezone(
            UUID tenantId
    ) {

        SchoolProfile profile =
                getForTenant(
                        tenantId
                );

        String timezone =
                profile.getTimezone();

        if (
                timezone == null
                || timezone.isBlank()
        ) {
            throw new IllegalStateException(
                    "School timezone is not configured"
            );
        }

        try {
            return ZoneId.of(
                    timezone
            );
        } catch (DateTimeException exception) {
            throw new IllegalStateException(
                    "School timezone is invalid: "
                            + timezone,
                    exception
            );
        }
    }

    private String validateTimezone(
            String timezone
    ) {

        if (
                timezone == null
                || timezone.isBlank()
        ) {
            return null;
        }

        String normalized =
                timezone.trim();

        try {
            /*
             * IANA/Java zone validation, e.g. Africa/Kampala.
             */
            ZoneId.of(
                    normalized
            );
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    "Invalid school timezone: "
                            + normalized,
                    exception
            );
        }

        return normalized;
    }
}
