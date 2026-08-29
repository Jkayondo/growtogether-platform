package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionApplicantServiceTest {

    @Mock
    private AdmissionApplicantRepository repository;

    @Mock
    private AdmissionApplicationRepository applications;

    @Mock
    private SchoolProfileService schoolProfiles;

    @Mock
    private AdmissionApplication application;

    private AdmissionApplicantService service;

    @BeforeEach
    void setUp() {

        service =
                new AdmissionApplicantService(
                        repository,
                        applications,
                        schoolProfiles
                );
    }

    @Test
    void createsTenantScopedApplicantForDraftApplication() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        CreateAdmissionApplicantCommand command =
                command(
                        LocalDate.of(2018, 5, 10)
                );

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );

        when(
                repository.existsByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                false
        );

        when(
                schoolProfiles.requireTimezone(
                        tenantId
                )
        ).thenReturn(
                ZoneId.of("Africa/Kampala")
        );

        when(
                repository.save(
                        any(AdmissionApplicant.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionApplicant result =
                service.create(
                        tenantId,
                        applicationId,
                        command
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                applicationId,
                result.getAdmissionApplicationId()
        );

        assertEquals(
                "John",
                result.getFirstName()
        );

        assertEquals(
                "Kayondo",
                result.getLastName()
        );

        assertEquals(
                LocalDate.of(2018, 5, 10),
                result.getDateOfBirth()
        );

        assertEquals(
                "MALE",
                result.getGender()
        );

        assertEquals(
                "UGA",
                result.getNationalityCode()
        );

        assertEquals(
                "UGA",
                result.getCountryOfBirthCode()
        );

        verify(
                repository
        ).save(
                result
        );
    }

    @Test
    void rejectsApplicantWhenApplicationIsOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                LocalDate.of(
                                                        2018,
                                                        5,
                                                        10
                                                )
                                        )
                                )
                );

        assertEquals(
                "Admission application not found for tenant",
                exception.getMessage()
        );

        verifyNoInteractions(
                repository,
                schoolProfiles
        );
    }

    @Test
    void rejectsApplicantWhenApplicationHasLeftDraft() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "SUBMITTED"
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                LocalDate.of(
                                                        2018,
                                                        5,
                                                        10
                                                )
                                        )
                                )
                );

        assertEquals(
                "Applicant cannot be added after admission application leaves DRAFT",
                exception.getMessage()
        );

        verifyNoInteractions(
                repository,
                schoolProfiles
        );
    }

    @Test
    void rejectsSecondApplicantForSameApplication() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );

        when(
                repository.existsByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                true
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                LocalDate.of(
                                                        2018,
                                                        5,
                                                        10
                                                )
                                        )
                                )
                );

        assertEquals(
                "Admission applicant already exists for application",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );

        verifyNoInteractions(
                schoolProfiles
        );
    }

    @Test
    void rejectsFutureDateOfBirthUsingSchoolTimezone() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        ZoneId timezone =
                ZoneId.of(
                        "Africa/Kampala"
                );

        LocalDate futureDate =
                LocalDate.now(
                        timezone
                ).plusDays(1);

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );

        when(
                repository.existsByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                false
        );

        when(
                schoolProfiles.requireTimezone(
                        tenantId
                )
        ).thenReturn(
                timezone
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.create(
                                        tenantId,
                                        applicationId,
                                        command(
                                                futureDate
                                        )
                                )
                );

        assertEquals(
                "dateOfBirth must not be in the future",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void allowsDateOfBirthEqualToSchoolLocalToday() {

        UUID tenantId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        ZoneId timezone =
                ZoneId.of(
                        "Africa/Kampala"
                );

        LocalDate today =
                LocalDate.now(
                        timezone
                );

        when(
                applications.findByTenantIdAndId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                Optional.of(application)
        );

        when(
                application.getAdmissionStatus()
        ).thenReturn(
                "DRAFT"
        );

        when(
                repository.existsByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        applicationId
                )
        ).thenReturn(
                false
        );

        when(
                schoolProfiles.requireTimezone(
                        tenantId
                )
        ).thenReturn(
                timezone
        );

        when(
                repository.save(
                        any(AdmissionApplicant.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        AdmissionApplicant result =
                service.create(
                        tenantId,
                        applicationId,
                        command(
                                today
                        )
                );

        assertEquals(
                today,
                result.getDateOfBirth()
        );
    }

    @Test
    void rejectsMissingTenantBeforeRepositoryAccess() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                null,
                                UUID.randomUUID(),
                                command(
                                        LocalDate.of(
                                                2018,
                                                5,
                                                10
                                        )
                                )
                        )
        );

        verifyNoInteractions(
                applications,
                repository,
                schoolProfiles
        );
    }

    @Test
    void rejectsMissingApplicationIdBeforeRepositoryAccess() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                UUID.randomUUID(),
                                null,
                                command(
                                        LocalDate.of(
                                                2018,
                                                5,
                                                10
                                        )
                                )
                        )
        );

        verifyNoInteractions(
                applications,
                repository,
                schoolProfiles
        );
    }

    @Test
    void rejectsMissingCommandBeforeRepositoryAccess() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                null
                        )
        );

        verifyNoInteractions(
                applications,
                repository,
                schoolProfiles
        );
    }

    private CreateAdmissionApplicantCommand command(
            LocalDate dateOfBirth
    ) {

        return new CreateAdmissionApplicantCommand(
                " John ",
                null,
                " Kayondo ",
                null,
                dateOfBirth,
                "male",
                "uga",
                "uga",
                "English",
                null,
                null,
                null,
                "BC-001",
                null,
                null,
                null,
                null,
                null
        );
    }
}
