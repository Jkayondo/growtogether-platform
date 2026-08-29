package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceRepository;
import africa.growtogether.platform.connect.ConnectSpaceType;

import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolConnectSpaceResolverTest {

    @Mock
    private SchoolProfileService schoolProfiles;

    @Mock
    private ConnectSpaceRepository connectSpaces;

    private SchoolConnectSpaceResolver resolver;

    @BeforeEach
    void setUp() {

        resolver =
                new SchoolConnectSpaceResolver(
                        schoolProfiles,
                        connectSpaces
                );
    }

    @Test
    void resolvesCanonicalInstitutionSpaceFromSchoolProfileIdentity() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        SchoolProfile profile =
                mock(
                        SchoolProfile.class
                );

        ConnectSpace institutionSpace =
                mock(
                        ConnectSpace.class
                );

        when(
                profile.getId()
        ).thenReturn(
                schoolProfileId
        );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                profile
        );

        when(
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                SchoolConnectSpaceResolver.CONTEXT_TYPE,
                                schoolProfileId.toString()
                        )
        ).thenReturn(
                Optional.of(
                        institutionSpace
                )
        );

        ConnectSpace result =
                resolver.requireInstitutionSpace(
                        tenantId
                );

        assertSame(
                institutionSpace,
                result
        );

        verify(
                schoolProfiles
        ).getForTenant(
                tenantId
        );

        verify(
                connectSpaces
        ).findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                tenantId,
                ConnectSpaceType.INSTITUTION,
                "SCHOOL_PROFILE",
                schoolProfileId.toString()
        );
    }

    @Test
    void nullTenantIsRejectedBeforeAnyLookup() {

        assertThrows(
                NullPointerException.class,
                () -> resolver.requireInstitutionSpace(
                        null
                )
        );

        verifyNoInteractions(
                schoolProfiles,
                connectSpaces
        );
    }

    @Test
    void schoolProfileMustHavePersistentIdentifier() {

        UUID tenantId =
                UUID.randomUUID();

        SchoolProfile profile =
                mock(
                        SchoolProfile.class
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                profile
        );

        when(
                profile.getId()
        ).thenReturn(
                null
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> resolver.requireInstitutionSpace(
                                tenantId
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "persistent identifier"
                        )
        );

        verifyNoInteractions(
                connectSpaces
        );
    }

    @Test
    void missingCanonicalInstitutionSpaceFailsInsteadOfCreatingOne() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        SchoolProfile profile =
                mock(
                        SchoolProfile.class
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                profile
        );

        when(
                profile.getId()
        ).thenReturn(
                schoolProfileId
        );

        when(
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                "SCHOOL_PROFILE",
                                schoolProfileId.toString()
                        )
        ).thenReturn(
                Optional.empty()
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> resolver.requireInstitutionSpace(
                                tenantId
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "not provisioned"
                        )
        );

        verify(
                connectSpaces,
                never()
        ).save(
                any()
        );

        verify(
                connectSpaces,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void resolverNeverFallsBackToArbitraryTenantSpace() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        SchoolProfile profile =
                mock(
                        SchoolProfile.class
                );

        when(
                schoolProfiles.getForTenant(
                        tenantId
                )
        ).thenReturn(
                profile
        );

        when(
                profile.getId()
        ).thenReturn(
                schoolProfileId
        );

        when(
                connectSpaces
                        .findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
                                tenantId,
                                ConnectSpaceType.INSTITUTION,
                                "SCHOOL_PROFILE",
                                schoolProfileId.toString()
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                IllegalStateException.class,
                () -> resolver.requireInstitutionSpace(
                        tenantId
                )
        );

        /*
         * Important R1 safety rule:
         * never choose "the first" institution or tenant space when
         * the exact School Profile mapping is absent.
         */
        verify(
                connectSpaces,
                never()
        ).findAllByTenantId(
                any()
        );
    }
}
