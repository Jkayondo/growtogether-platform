package africa.growtogether.platform.school.profile;

import africa.growtogether.platform.school.integration.SchoolConnectSpaceProvisioningService;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SchoolProfileServiceTest {

    @Test
    void createsTenantOwnedSchoolProfileWithValidTimezone() {

        SchoolProfileRepository repository =
                mock(SchoolProfileRepository.class);

        SchoolConnectSpaceProvisioningService connectProvisioning =
                mock(
                        SchoolConnectSpaceProvisioningService.class
                );

        SchoolProfileService service =
                new SchoolProfileService(
                        repository,
                        connectProvisioning
                );

        UUID tenantId =
                UUID.randomUUID();

        when(
                repository.existsByTenantId(
                        tenantId
                )
        ).thenReturn(false);

        when(
                repository.saveAndFlush(
                        any(SchoolProfile.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        SchoolProfile result =
                service.create(
                        tenantId,
                        command(
                                "Africa/Kampala"
                        )
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                "Africa/Kampala",
                result.getTimezone()
        );

        assertEquals(
                "GT-SCHOOL",
                result.getSchoolCode()
        );

        verify(
                repository
        ).saveAndFlush(
                any(SchoolProfile.class)
        );

        verify(
                connectProvisioning
        ).provisionInstitutionSpace(
                tenantId,
                result
        );
    }

    @Test
    void rejectsSecondSchoolProfileForSameTenant() {

        SchoolProfileRepository repository =
                mock(SchoolProfileRepository.class);

        SchoolConnectSpaceProvisioningService connectProvisioning =
                mock(
                        SchoolConnectSpaceProvisioningService.class
                );

        SchoolProfileService service =
                new SchoolProfileService(
                        repository,
                        connectProvisioning
                );

        UUID tenantId =
                UUID.randomUUID();

        when(
                repository.existsByTenantId(
                        tenantId
                )
        ).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command(
                                        "Africa/Kampala"
                                )
                        )
                );

        assertEquals(
                "School profile already exists for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).saveAndFlush(
                any(SchoolProfile.class)
        );


        verifyNoInteractions(
                connectProvisioning
        );
    }

    @Test
    void rejectsInvalidSchoolTimezone() {

        SchoolProfileRepository repository =
                mock(SchoolProfileRepository.class);

        SchoolConnectSpaceProvisioningService connectProvisioning =
                mock(
                        SchoolConnectSpaceProvisioningService.class
                );

        SchoolProfileService service =
                new SchoolProfileService(
                        repository,
                        connectProvisioning
                );

        UUID tenantId =
                UUID.randomUUID();

        when(
                repository.existsByTenantId(
                        tenantId
                )
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command(
                                        "Kampala Time"
                                )
                        )
                );

        assertEquals(
                "Invalid school timezone: Kampala Time",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).saveAndFlush(
                any(SchoolProfile.class)
        );


        verifyNoInteractions(
                connectProvisioning
        );
    }

    @Test
    void getsSchoolProfileUsingTenantBoundary() {

        SchoolProfileRepository repository =
                mock(SchoolProfileRepository.class);

        SchoolConnectSpaceProvisioningService connectProvisioning =
                mock(
                        SchoolConnectSpaceProvisioningService.class
                );

        SchoolProfileService service =
                new SchoolProfileService(
                        repository,
                        connectProvisioning
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID profileId =
                UUID.randomUUID();

        SchoolProfile profile =
                mock(SchoolProfile.class);

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        profileId
                )
        ).thenReturn(
                Optional.of(profile)
        );

        SchoolProfile result =
                service.get(
                        tenantId,
                        profileId
                );

        assertSame(
                profile,
                result
        );

        verify(
                repository
        ).findByTenantIdAndId(
                tenantId,
                profileId
        );

        verify(
                repository,
                never()
        ).findById(
                profileId
        );
    }

    @Test
    void resolvesConfiguredSchoolTimezoneAsZoneId() {

        SchoolProfileRepository repository =
                mock(SchoolProfileRepository.class);

        SchoolConnectSpaceProvisioningService connectProvisioning =
                mock(
                        SchoolConnectSpaceProvisioningService.class
                );

        SchoolProfileService service =
                new SchoolProfileService(
                        repository,
                        connectProvisioning
                );

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile profile =
                mock(SchoolProfile.class);

        when(
                profile.getTimezone()
        ).thenReturn(
                "Africa/Kampala"
        );

        when(
                repository.findByTenantId(
                        tenantId
                )
        ).thenReturn(
                Optional.of(profile)
        );

        ZoneId zoneId =
                service.requireTimezone(
                        tenantId
                );

        assertEquals(
                ZoneId.of("Africa/Kampala"),
                zoneId
        );
    }

    private static CreateSchoolProfileCommand command(
            String timezone
    ) {

        return new CreateSchoolProfileCommand(
                "GT-SCHOOL",
                "GrowTogether Demonstration School",
                "GrowTogether Demonstration School Ltd",
                "Uganda",
                "UG",
                "UGX",
                timezone,
                "school@growtogether.africa",
                "+256700000000",
                "https://growtogether.africa"
        );
    }
}
