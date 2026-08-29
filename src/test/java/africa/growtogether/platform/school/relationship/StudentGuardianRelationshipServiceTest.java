package africa.growtogether.platform.school.relationship;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.guardian.Guardian;
import africa.growtogether.platform.school.guardian.GuardianRepository;

import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentGuardianRelationshipServiceTest {

    @Test
    void createsRelationshipAndPersistsGuardianResponsibilities() {

        Fixture f = new Fixture();

        f.stubStudentAndGuardian();

        when(
                f.repository.existsByTenantIdAndStudentIdAndGuardianId(
                        f.tenantId,
                        f.studentId,
                        f.guardianId
                )
        ).thenReturn(false);

        when(
                f.repository
                        .findFirstByTenantIdAndStudentIdAndPrimaryGuardianTrueAndRelationshipStatusAndStatus(
                                f.tenantId,
                                f.studentId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                f.repository.save(
                        any(StudentGuardianRelationship.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        StudentGuardianRelationship result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                "MOTHER",
                                true
                        )
                );

        assertNotNull(result);

        assertEquals(
                f.studentId,
                result.getStudentId()
        );

        assertEquals(
                f.guardianId,
                result.getGuardianId()
        );

        assertEquals(
                "MOTHER",
                result.getRelationshipType()
        );

        assertEquals(
                "Biological mother",
                result.getRelationshipDescription()
        );

        assertTrue(
                result.isLegalGuardian()
        );

        assertTrue(
                result.isPrimaryGuardian()
        );

        assertTrue(
                result.isEmergencyContact()
        );

        assertTrue(
                result.isHasCustody()
        );

        assertEquals(
                "JOINT",
                result.getCustodyType()
        );

        assertTrue(
                result.isLivesWithStudent()
        );

        assertTrue(
                result.isAuthorizedToCollect()
        );

        assertTrue(
                result.isReceivesCommunications()
        );

        assertTrue(
                result.isReceivesAcademicInformation()
        );

        assertTrue(
                result.isReceivesDisciplineInformation()
        );

        assertTrue(
                result.isReceivesMedicalInformation()
        );

        assertTrue(
                result.isMayApproveSchoolActivities()
        );

        assertEquals(
                "ACTIVE",
                result.getRelationshipStatus()
        );

        verify(
                f.repository
        ).save(
                any(StudentGuardianRelationship.class)
        );
    }

    @Test
    void rejectsStudentOutsideTenantBoundary() {

        Fixture f = new Fixture();

        when(
                f.students.findByTenantIdAndId(
                        f.tenantId,
                        f.studentId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "MOTHER",
                                        false
                                )
                        )
                );

        assertEquals(
                "Student not found for tenant",
                error.getMessage()
        );

        verifyNoInteractions(
                f.guardians
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentGuardianRelationship.class)
        );
    }

    @Test
    void rejectsGuardianOutsideTenantBoundary() {

        Fixture f = new Fixture();

        when(
                f.students.findByTenantIdAndId(
                        f.tenantId,
                        f.studentId
                )
        ).thenReturn(
                Optional.of(
                        mock(Student.class)
                )
        );

        when(
                f.guardians.findByTenantIdAndId(
                        f.tenantId,
                        f.guardianId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "FATHER",
                                        false
                                )
                        )
                );

        assertEquals(
                "Guardian not found for tenant",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentGuardianRelationship.class)
        );
    }

    @Test
    void rejectsDuplicateStudentGuardianRelationship() {

        Fixture f = new Fixture();

        f.stubStudentAndGuardian();

        when(
                f.repository.existsByTenantIdAndStudentIdAndGuardianId(
                        f.tenantId,
                        f.studentId,
                        f.guardianId
                )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "MOTHER",
                                        false
                                )
                        )
                );

        assertEquals(
                "Student guardian relationship already exists",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentGuardianRelationship.class)
        );
    }

    @Test
    void rejectsSecondActivePrimaryGuardian() {

        Fixture f = new Fixture();

        f.stubStudentAndGuardian();

        when(
                f.repository.existsByTenantIdAndStudentIdAndGuardianId(
                        f.tenantId,
                        f.studentId,
                        f.guardianId
                )
        ).thenReturn(
                false
        );

        when(
                f.repository
                        .findFirstByTenantIdAndStudentIdAndPrimaryGuardianTrueAndRelationshipStatusAndStatus(
                                f.tenantId,
                                f.studentId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(
                        mock(StudentGuardianRelationship.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "MOTHER",
                                        true
                                )
                        )
                );

        assertEquals(
                "Student already has an active primary guardian",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentGuardianRelationship.class)
        );
    }

    @Test
    void rejectsInvalidRelationshipType() {

        Fixture f = new Fixture();

        f.stubStudentAndGuardian();

        when(
                f.repository.existsByTenantIdAndStudentIdAndGuardianId(
                        f.tenantId,
                        f.studentId,
                        f.guardianId
                )
        ).thenReturn(
                false
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "INVALID_RELATIONSHIP",
                                        false
                                )
                        )
                );

        assertEquals(
                "Invalid relationship type: INVALID_RELATIONSHIP",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(StudentGuardianRelationship.class)
        );
    }

    @Test
    void endsRelationshipAndRecordsEffectiveDate() {

        Fixture f = new Fixture();

        UUID relationshipId =
                UUID.randomUUID();

        StudentGuardianRelationship relationship =
                new StudentGuardianRelationship(
                        f.studentId,
                        f.guardianId,
                        "MOTHER",
                        "Biological mother",
                        true,
                        true,
                        true,
                        true,
                        "JOINT",
                        null,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true
                );

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        relationshipId
                )
        ).thenReturn(
                Optional.of(relationship)
        );

        when(
                f.repository.save(
                        relationship
                )
        ).thenReturn(
                relationship
        );

        LocalDate effectiveTo =
                LocalDate.now().plusDays(30);

        StudentGuardianRelationship result =
                f.service.end(
                        f.tenantId,
                        relationshipId,
                        effectiveTo
                );

        assertEquals(
                "ENDED",
                result.getRelationshipStatus()
        );

        assertEquals(
                effectiveTo,
                result.getEffectiveTo()
        );

        verify(
                f.repository
        ).findByTenantIdAndId(
                f.tenantId,
                relationshipId
        );

        verify(
                f.repository
        ).save(
                relationship
        );
    }

    private static class Fixture {

        final StudentGuardianRelationshipRepository repository =
                mock(StudentGuardianRelationshipRepository.class);

        final StudentRepository students =
                mock(StudentRepository.class);

        final GuardianRepository guardians =
                mock(GuardianRepository.class);

        final StudentGuardianRelationshipService service =
                new StudentGuardianRelationshipService(
                        repository,
                        students,
                        guardians
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID studentId =
                UUID.randomUUID();

        final UUID guardianId =
                UUID.randomUUID();

        void stubStudentAndGuardian() {

            when(
                    students.findByTenantIdAndId(
                            tenantId,
                            studentId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Student.class)
                    )
            );

            when(
                    guardians.findByTenantIdAndId(
                            tenantId,
                            guardianId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(Guardian.class)
                    )
            );
        }

        CreateStudentGuardianRelationshipCommand command(
                String relationshipType,
                boolean primaryGuardian
        ) {

            return new CreateStudentGuardianRelationshipCommand(
                    studentId,
                    guardianId,
                    relationshipType,
                    "Biological mother",
                    true,
                    primaryGuardian,
                    true,
                    true,
                    "JOINT",
                    "Joint parental custody",
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true
            );
        }
    }
}
