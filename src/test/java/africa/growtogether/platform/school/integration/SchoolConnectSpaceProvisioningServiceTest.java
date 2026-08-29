package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;

import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceRepository;
import africa.growtogether.platform.connect.ConnectSpaceType;

import africa.growtogether.platform.school.profile.SchoolProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolConnectSpaceProvisioningServiceTest {

    @Mock
    private ConnectSpaceRepository connectSpaces;

    @Mock
    private SchoolConnectAdminMembershipProvisioningService
            adminMemberships;

    private SchoolConnectSpaceProvisioningService service;

    @BeforeEach
    void setUp() {

        service =
                new SchoolConnectSpaceProvisioningService(
                        connectSpaces,
                        adminMemberships
                );
    }

    @Test
    void createsCanonicalInstitutionSpaceForPersistedSchoolProfile() {

        UUID tenantId =
                UUID.randomUUID();

        UUID profileId =
                UUID.randomUUID();

        SchoolProfile profile =
                profile(
                        tenantId,
                        profileId,
                        "Pio and Pretty International School"
                );

        when(
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                SchoolConnectSpaceResolver.CONTEXT_TYPE,
                                profileId.toString()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                connectSpaces.saveAndFlush(
                        any(ConnectSpace.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ConnectSpace result =
                service.provisionInstitutionSpace(
                        tenantId,
                        profile
                );

        ArgumentCaptor<ConnectSpace> captured =
                ArgumentCaptor.forClass(
                        ConnectSpace.class
                );

        verify(
                connectSpaces
        ).saveAndFlush(
                captured.capture()
        );

        ConnectSpace created =
                captured.getValue();

        assertSame(
                created,
                result
        );

        assertEquals(
                tenantId,
                created.getTenantId()
        );

        assertEquals(
                ConnectSpaceType.INSTITUTION,
                created.getSpaceType()
        );

        assertEquals(
                "Pio and Pretty International School",
                created.getName()
        );

        assertEquals(
                "SCHOOL_PROFILE",
                created.getContextType()
        );

        assertEquals(
                profileId.toString(),
                created.getContextReference()
        );

        verify(
                adminMemberships
        ).ensureSchoolAdministrators(
                tenantId,
                created
        );
    }

    @Test
    void existingCanonicalSpaceIsReusedIdempotently() {

        UUID tenantId =
                UUID.randomUUID();

        UUID profileId =
                UUID.randomUUID();

        SchoolProfile profile =
                profile(
                        tenantId,
                        profileId,
                        "Existing School"
                );

        ConnectSpace existing =
                mock(
                        ConnectSpace.class
                );

        when(
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                SchoolConnectSpaceResolver.CONTEXT_TYPE,
                                profileId.toString()
                        )
        ).thenReturn(
                Optional.of(
                        existing
                )
        );

        ConnectSpace result =
                service.provisionInstitutionSpace(
                        tenantId,
                        profile
                );

        assertSame(
                existing,
                result
        );

        verify(
                connectSpaces,
                never()
        ).saveAndFlush(
                any()
        );

        verify(
                adminMemberships
        ).ensureSchoolAdministrators(
                tenantId,
                existing
        );
    }

    @Test
    void profileTenantMustMatchProvisioningTenant() {

        UUID profileTenant =
                UUID.randomUUID();

        UUID requestedTenant =
                UUID.randomUUID();

        SchoolProfile profile =
                profile(
                        profileTenant,
                        UUID.randomUUID(),
                        "Wrong Tenant School"
                );

        assertThrows(
                TenantScopeViolationException.class,
                () -> service.provisionInstitutionSpace(
                        requestedTenant,
                        profile
                )
        );

        verifyNoInteractions(
                connectSpaces
        );
    }

    @Test
    void profileMustBePersistedBeforeProvisioning() {

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile profile =
                profile(
                        tenantId,
                        null,
                        "Unpersisted School"
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.provisionInstitutionSpace(
                                tenantId,
                                profile
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "persisted"
                        )
        );

        verifyNoInteractions(
                connectSpaces
        );
    }

    @Test
    void exactCanonicalLookupIsAlwaysUsedBeforeCreation() {

        UUID tenantId =
                UUID.randomUUID();

        UUID profileId =
                UUID.randomUUID();

        SchoolProfile profile =
                profile(
                        tenantId,
                        profileId,
                        "Canonical Lookup School"
                );

        when(
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                SchoolConnectSpaceResolver.CONTEXT_TYPE,
                                profileId.toString()
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                connectSpaces.saveAndFlush(
                        any(ConnectSpace.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        service.provisionInstitutionSpace(
                tenantId,
                profile
        );

        verify(
                connectSpaces
        ).findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                tenantId,
                ConnectSpaceType.INSTITUTION,
                "SCHOOL_PROFILE",
                profileId.toString()
        );

        /*
         * Provisioning must never reuse an arbitrary tenant space.
         */
        verify(
                connectSpaces,
                never()
        ).findAllByTenantId(
                any()
        );
    }

    private SchoolProfile profile(
            UUID tenantId,
            UUID id,
            String schoolName
    ) {

        SchoolProfile profile =
                mock(
                        SchoolProfile.class
                );

        when(
                profile.getTenantId()
        ).thenReturn(
                tenantId
        );

        lenient()
                .when(
                        profile.getId()
                )
                .thenReturn(
                        id
                );

        /*
         * Only required when a new space is actually created.
         * lenient avoids unnecessary-stubbing failures in rejection/reuse tests.
         */
        lenient()
                .when(
                        profile.getSchoolName()
                )
                .thenReturn(
                        schoolName
                );

        return profile;
    }
}
