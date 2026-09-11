package africa.growtogether.platform.school.student;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.admission.AdmissionApplication;
import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;
import africa.growtogether.platform.school.profile.SchoolProfile;
import africa.growtogether.platform.school.profile.SchoolProfileRepository;
import africa.growtogether.platform.school.student.identity.LearnerIdentityGenerator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceTest {


    private static final String GENERATED_STUDENT_NUMBER =
            "PIO-2026-001";


    private static final String GENERATED_PERMANENT_LEARNER_NUMBER =
            "GT-LRN-000000001";

    @Test
    void createsStudentWithinTenantBoundary() {

        StudentRepository repository =
                mock(StudentRepository.class);

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

        UUID tenantId = UUID.randomUUID();

        configureSchoolProfile(
                schoolProfiles,
                tenantId
        );

        CreateStudentCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        GENERATED_STUDENT_NUMBER
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        GENERATED_PERMANENT_LEARNER_NUMBER
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
                GENERATED_STUDENT_NUMBER,
                result.getStudentNumber()
        );

        assertEquals(
                GENERATED_PERMANENT_LEARNER_NUMBER,
                result.getPermanentLearnerNumber()
        );

        verify(
                repository
        ).existsByTenantIdAndStudentNumber(
                tenantId,
                GENERATED_STUDENT_NUMBER
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

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

        UUID tenantId = UUID.randomUUID();

        configureSchoolProfile(
                schoolProfiles,
                tenantId
        );

        CreateStudentCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        GENERATED_STUDENT_NUMBER
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

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

        UUID tenantId = UUID.randomUUID();

        configureSchoolProfile(
                schoolProfiles,
                tenantId
        );

        CreateStudentCommand command =
                defaultCommand(null);

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        GENERATED_STUDENT_NUMBER
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        GENERATED_PERMANENT_LEARNER_NUMBER
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
    void rejectsAdmissionApplicationOutsideTenantBoundary() {

        StudentRepository repository =
                mock(StudentRepository.class);

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

        UUID tenantId =
                UUID.randomUUID();

        configureSchoolProfile(
                schoolProfiles,
                tenantId
        );

        UUID admissionApplicationId =
                UUID.randomUUID();

        CreateStudentCommand command =
                defaultCommand(
                        admissionApplicationId
                );

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        GENERATED_STUDENT_NUMBER
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        GENERATED_PERMANENT_LEARNER_NUMBER
                )
        ).thenReturn(false);

        when(
                admissionApplications.findByTenantIdAndId(
                        tenantId,
                        admissionApplicationId
                )
        ).thenReturn(
                Optional.empty()
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
                "Admission application not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).findByTenantIdAndAdmissionApplicationId(
                any(),
                any()
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

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

        UUID tenantId =
                UUID.randomUUID();

        configureSchoolProfile(
                schoolProfiles,
                tenantId
        );

        UUID admissionApplicationId =
                UUID.randomUUID();

        CreateStudentCommand command =
                defaultCommand(
                        admissionApplicationId
                );

        when(
                repository.existsByTenantIdAndStudentNumber(
                        tenantId,
                        GENERATED_STUDENT_NUMBER
                )
        ).thenReturn(false);

        when(
                repository.existsByPermanentLearnerNumber(
                        GENERATED_PERMANENT_LEARNER_NUMBER
                )
        ).thenReturn(false);

        when(
                admissionApplications.findByTenantIdAndId(
                        tenantId,
                        admissionApplicationId
                )
        ).thenReturn(
                Optional.of(
                        mock(AdmissionApplication.class)
                )
        );

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

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

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

    @Test
    void listsActiveStudentsUsingTenantScopedRecordStatus() {

        StudentRepository repository =
                mock(StudentRepository.class);

        AdmissionApplicationRepository admissionApplications =
                mock(AdmissionApplicationRepository.class);

        LearnerIdentityGenerator identityGenerator =
                mock(LearnerIdentityGenerator.class);

        SchoolProfileRepository schoolProfiles =
                mock(SchoolProfileRepository.class);

        configureIdentityGenerator(
                identityGenerator
        );

        StudentService service =
                new StudentService(
                        repository,
                        admissionApplications,
                        identityGenerator,
                        schoolProfiles
                );

        UUID tenantId =
                UUID.randomUUID();

        List<Student> students =
                List.of(
                        mock(Student.class),
                        mock(Student.class)
                );

        when(
                repository.findByTenantIdAndStatus(
                        tenantId,
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                students
        );

        List<Student> result =
                service.findActiveStudents(
                        tenantId
                );

        assertSame(
                students,
                result
        );

        verify(
                repository
        ).findByTenantIdAndStatus(
                tenantId,
                EntityStatus.ACTIVE
        );
    }

    private void configureIdentityGenerator(
            LearnerIdentityGenerator identityGenerator
    ) {

        when(
                identityGenerator.generatePermanentLearnerNumber()
        )
                .thenReturn(
                        GENERATED_PERMANENT_LEARNER_NUMBER
                );


        when(
                identityGenerator.generateStudentNumber(
                        any(),
                        anyString(),
                        anyInt()
                )
        )
                .thenReturn(
                        GENERATED_STUDENT_NUMBER
                );

    }



    private void configureSchoolProfile(
            SchoolProfileRepository schoolProfiles,
            UUID tenantId
    ) {

        SchoolProfile profile =
                new SchoolProfile(
                        "PIO",
                        "Pio and Pretty International School",
                        null,
                        "PRIMARY",
                        "UG",
                        "UGX",
                        "Africa/Kampala",
                        null,
                        null,
                        null
                );


        when(
                schoolProfiles.findByTenantId(
                        tenantId
                )
        ).thenReturn(
                Optional.of(profile)
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
