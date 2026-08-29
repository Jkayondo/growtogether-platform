package africa.growtogether.platform.school.guardian;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GuardianServiceTest {

    @Test
    void createsGuardianWithinTenantBoundary() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        CreateGuardianCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndGuardianNumber(
                        tenantId,
                        command.guardianNumber()
                )
        ).thenReturn(false);

        when(
                repository.save(
                        any(Guardian.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        Guardian result =
                service.create(
                        tenantId,
                        command
                );

        assertNotNull(result);

        assertEquals(
                command.guardianNumber(),
                result.getGuardianNumber()
        );

        assertEquals(
                command.firstName(),
                result.getFirstName()
        );

        assertEquals(
                command.lastName(),
                result.getLastName()
        );

        assertEquals(
                "UNVERIFIED",
                result.getVerificationStatus()
        );

        assertEquals(
                "ACTIVE",
                result.getGuardianStatus()
        );

        verify(
                repository
        ).existsByTenantIdAndGuardianNumber(
                tenantId,
                command.guardianNumber()
        );

        verify(
                repository
        ).save(
                any(Guardian.class)
        );
    }

    @Test
    void rejectsDuplicateGuardianNumberWithinTenant() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        CreateGuardianCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndGuardianNumber(
                        tenantId,
                        command.guardianNumber()
                )
        ).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command
                        )
                );

        assertEquals(
                "Guardian number already exists for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Guardian.class)
        );
    }

    @Test
    void rejectsDuplicateAdmissionGuardianWithinTenant() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        UUID sourceAdmissionGuardianId =
                UUID.randomUUID();

        CreateGuardianCommand command =
                defaultCommand(
                        sourceAdmissionGuardianId
                );

        when(
                repository.existsByTenantIdAndGuardianNumber(
                        tenantId,
                        command.guardianNumber()
                )
        ).thenReturn(false);

        when(
                repository.existsByTenantIdAndSourceAdmissionGuardianId(
                        tenantId,
                        sourceAdmissionGuardianId
                )
        ).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command
                        )
                );

        assertEquals(
                "Admission guardian already has a guardian profile",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Guardian.class)
        );
    }

    @Test
    void retrievesGuardianUsingTenantScopedId() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        Guardian guardian =
                mock(Guardian.class);

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                Optional.of(guardian)
        );

        Guardian result =
                service.get(
                        tenantId,
                        guardianId
                );

        assertSame(
                guardian,
                result
        );

        verify(
                repository
        ).findByTenantIdAndId(
                tenantId,
                guardianId
        );

        verify(
                repository,
                never()
        ).findById(
                guardianId
        );
    }

    @Test
    void verifiesGuardianAndRecordsVerifierAndTimestamp() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        UUID verifiedBy =
                UUID.randomUUID();

        Guardian guardian =
                guardian();

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                Optional.of(guardian)
        );

        when(
                repository.save(guardian)
        ).thenReturn(
                guardian
        );

        Guardian result =
                service.verify(
                        tenantId,
                        guardianId,
                        verifiedBy
                );

        assertEquals(
                "VERIFIED",
                result.getVerificationStatus()
        );

        assertEquals(
                verifiedBy,
                result.getVerifiedBy()
        );

        assertNotNull(
                result.getVerifiedAt()
        );

        verify(
                repository
        ).save(
                guardian
        );
    }

    @Test
    void rejectsVerificationWithoutVerifier() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        Guardian guardian =
                guardian();

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        guardianId
                )
        ).thenReturn(
                Optional.of(guardian)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.verify(
                                tenantId,
                                guardianId,
                                null
                        )
                );

        assertEquals(
                "verifiedBy must not be null",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Guardian.class)
        );
    }

    @Test
    void rejectsFutureGuardianBirthDate() {

        GuardianRepository repository =
                mock(GuardianRepository.class);

        GuardianService service =
                new GuardianService(repository);

        UUID tenantId =
                UUID.randomUUID();

        CreateGuardianCommand command =
                new CreateGuardianCommand(
                        "GDN-002",
                        "Sarah",
                        null,
                        "Nakato",
                        null,
                        LocalDate.now().plusDays(1),
                        "FEMALE",
                        "UG",
                        null,
                        null,
                        "+256700000002",
                        null,
                        null,
                        "Kampala",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "English"
                );

        when(
                repository.existsByTenantIdAndGuardianNumber(
                        tenantId,
                        command.guardianNumber()
                )
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command
                        )
                );

        assertEquals(
                "dateOfBirth must not be in the future",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Guardian.class)
        );
    }

    private CreateGuardianCommand defaultCommand(
            UUID sourceAdmissionGuardianId
    ) {

        return new CreateGuardianCommand(
                "GDN-001",
                "Mary",
                null,
                "Nabirye",
                "Mary",
                LocalDate.of(1985, 4, 15),
                "FEMALE",
                "UG",
                null,
                null,
                "+256700000001",
                null,
                "mary@example.com",
                "Kampala",
                null,
                "Teacher",
                null,
                null,
                sourceAdmissionGuardianId,
                "English"
        );
    }

    private Guardian guardian() {

        return new Guardian(
                "GDN-001",
                "Mary",
                null,
                "Nabirye",
                "Mary",
                LocalDate.of(1985, 4, 15),
                "FEMALE",
                "UG",
                null,
                null,
                "+256700000001",
                null,
                "mary@example.com",
                "Kampala",
                null,
                "Teacher",
                null,
                null,
                null,
                "English"
        );
    }
}
