package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CampusServiceTest {

    @Mock
    private CampusRepository repository;

    @Mock
    private SchoolProfileService schoolProfiles;

    @InjectMocks
    private CampusService service;


    @Test
    void createRequiresSchoolProfileOwnedByTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID schoolProfileId = UUID.randomUUID();

        SchoolProfile schoolProfile =
                org.mockito.Mockito.mock(
                        SchoolProfile.class
                );

        when(
                schoolProfiles.get(
                        tenantId,
                        schoolProfileId
                )
        ).thenReturn(
                schoolProfile
        );

        when(
                repository.save(
                        any(Campus.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        Campus created =
                service.create(
                        tenantId,
                        schoolProfileId,
                        "MAIN",
                        "Main Campus",
                        "Plot 1",
                        "Wakiso",
                        "Entebbe",
                        "UG",
                        "+256700000000",
                        "school@example.com",
                        false
                );

        verify(
                schoolProfiles
        ).get(
                tenantId,
                schoolProfileId
        );

        ArgumentCaptor<Campus> captor =
                ArgumentCaptor.forClass(
                        Campus.class
                );

        verify(
                repository
        ).save(
                captor.capture()
        );

        Campus saved =
                captor.getValue();

        assertEquals(
                tenantId,
                saved.getTenantId()
        );

        assertEquals(
                schoolProfileId,
                saved.getSchoolProfileId()
        );

        assertEquals(
                "MAIN",
                saved.getCampusCode()
        );

        assertEquals(
                "Main Campus",
                saved.getCampusName()
        );

        assertFalse(
                saved.isMainCampus()
        );

        assertSame(
                saved,
                created
        );
    }


    @Test
    void createRejectsSchoolProfileOutsideTenantAndDoesNotSave() {

        UUID tenantId = UUID.randomUUID();
        UUID schoolProfileId = UUID.randomUUID();

        doThrow(
                new IllegalArgumentException(
                        "School profile not found for tenant"
                )
        ).when(
                schoolProfiles
        ).get(
                tenantId,
                schoolProfileId
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                schoolProfileId,
                                "MAIN",
                                "Main Campus",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                false
                        )
        );

        verify(
                repository,
                never()
        ).save(
                any(Campus.class)
        );
    }


    @Test
    void findBySchoolProfileUsesTenantScopedRepositoryQuery() {

        UUID tenantId = UUID.randomUUID();
        UUID schoolProfileId = UUID.randomUUID();

        List<Campus> expected =
                List.of();

        when(
                repository
                        .findByTenantIdAndSchoolProfileId(
                                tenantId,
                                schoolProfileId
                        )
        ).thenReturn(
                expected
        );

        List<Campus> result =
                service.findBySchoolProfile(
                        tenantId,
                        schoolProfileId
                );

        assertSame(
                expected,
                result
        );

        verify(
                repository
        ).findByTenantIdAndSchoolProfileId(
                tenantId,
                schoolProfileId
        );
    }


    @Test
    void findByCodeUsesTenantScopedRepositoryQuery() {

        UUID tenantId = UUID.randomUUID();

        Campus campus =
                org.mockito.Mockito.mock(
                        Campus.class
                );

        when(
                repository.findByTenantIdAndCampusCode(
                        tenantId,
                        "MAIN"
                )
        ).thenReturn(
                java.util.Optional.of(
                        campus
                )
        );

        Campus result =
                service.findByCode(
                        tenantId,
                        "MAIN"
                );

        assertSame(
                campus,
                result
        );

        verify(
                repository
        ).findByTenantIdAndCampusCode(
                tenantId,
                "MAIN"
        );
    }
}
