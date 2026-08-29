package africa.growtogether.platform.school.student;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Test
    void createsStudentWithinTenantBoundary() {

        StudentRepository repository =
                mock(StudentRepository.class);

        StudentService service =
                new StudentService(repository);

        UUID tenantId = UUID.randomUUID();

        CreateStudentCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        command.studentNumber()
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        command.permanentLearnerNumber()
                )
        ).thenReturn(false);

        when(
                repository.save(
                        any(Student.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        Student result =
                service.create(
                        tenantId,
                        command
                );

        assertNotNull(result);

        assertEquals(
                command.studentNumber(),
                result.getStudentNumber()
        );

        assertEquals(
                command.permanentLearnerNumber(),
                result.getPermanentLearnerNumber()
        );

        verify(
                repository
        ).existsByTenantIdAndStudentNumber(
                tenantId,
                command.studentNumber()
        );

        verify(
                repository
        ).save(
                any(Student.class)
        );
    }

    @Test
    void rejectsDuplicateStudentNumberWithinTenant() {

        StudentRepository repository =
                mock(StudentRepository.class);

        StudentService service =
                new StudentService(repository);

        UUID tenantId = UUID.randomUUID();

        CreateStudentCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        command.studentNumber()
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
                "Student number already exists for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Student.class)
        );
    }

    @Test
    void rejectsDuplicatePermanentLearnerNumberGlobally() {

        StudentRepository repository =
                mock(StudentRepository.class);

        StudentService service =
                new StudentService(repository);

        UUID tenantId = UUID.randomUUID();

        CreateStudentCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        command.studentNumber()
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        command.permanentLearnerNumber()
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
                "Permanent learner number already exists",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Student.class)
        );
    }

    @Test
    void rejectsAdmissionApplicationAlreadyLinkedToStudent() {

        StudentRepository repository =
                mock(StudentRepository.class);

        StudentService service =
                new StudentService(repository);

        UUID tenantId =
                UUID.randomUUID();

        UUID admissionApplicationId =
                UUID.randomUUID();

        CreateStudentCommand command =
                defaultCommand(
                        admissionApplicationId
                );

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        command.studentNumber()
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        command.permanentLearnerNumber()
                )
        ).thenReturn(false);

        when(
                repository.findByTenantIdAndAdmissionApplicationId(
                        tenantId,
                        admissionApplicationId
                )
        ).thenReturn(
                Optional.of(
                        mock(Student.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command
                        )
                );

        assertEquals(
                "Admission application already has a student profile",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(Student.class)
        );
    }

    @Test
    void retrievesStudentUsingTenantScopedId() {

        StudentRepository repository =
                mock(StudentRepository.class);

        StudentService service =
                new StudentService(repository);

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        Student student =
                mock(Student.class);

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        studentId
                )
        ).thenReturn(
                Optional.of(student)
        );

        Student result =
                service.get(
                        tenantId,
                        studentId
                );

        assertSame(
                student,
                result
        );

        verify(
                repository
        ).findByTenantIdAndId(
                tenantId,
                studentId
        );

        verify(
                repository,
                never()
        ).findById(
                studentId
        );
    }

    private CreateStudentCommand defaultCommand(
            UUID admissionApplicationId
    ) {

        return new CreateStudentCommand(
                admissionApplicationId,
                "STU-001",
                "GT-LRN-000001",
                "Grace",
                null,
                "Namutebi",
                "Grace",
                LocalDate.of(2015, 5, 10),
                "FEMALE",
                "UG",
                "UG",
                "English",
                null,
                null,
                null,
                "Kampala",
                null,
                null,
                LocalDate.of(2026, 1, 20),
                null,
                null,
                null
        );
    }
}
