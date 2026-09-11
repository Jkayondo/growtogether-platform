package africa.growtogether.platform.school.student;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.admission.AdmissionApplicationRepository;
import africa.growtogether.platform.school.profile.SchoolProfileRepository;
import africa.growtogether.platform.school.student.identity.LearnerIdentityGenerator;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final AdmissionApplicationRepository admissionApplications;
    private final LearnerIdentityGenerator identityGenerator;
    private final SchoolProfileRepository schoolProfiles;

    public StudentService(
            StudentRepository repository,
            AdmissionApplicationRepository admissionApplications,
            LearnerIdentityGenerator identityGenerator,
            SchoolProfileRepository schoolProfiles
    ) {
        this.repository = repository;
        this.admissionApplications = admissionApplications;
        this.identityGenerator = identityGenerator;
        this.schoolProfiles = schoolProfiles;
    }

    @Transactional
    public Student create(
            UUID tenantId,
            CreateStudentCommand command
    ) {

        String permanentLearnerNumber =
                identityGenerator.generatePermanentLearnerNumber();


        String schoolCode =
                schoolProfiles.findByTenantId(
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "School profile not found for tenant"
                        )
                )
                .getSchoolCode();


        String studentNumber =
                identityGenerator.generateStudentNumber(
                        tenantId,
                        schoolCode,
                        command.admissionDate() != null
                                ? command.admissionDate().getYear()
                                : java.time.LocalDate.now().getYear()
                );


        if (repository.existsByTenantIdAndStudentNumber(
                tenantId,
                studentNumber
        )) {
            throw new IllegalArgumentException(
                    "Student number already exists for tenant"
            );
        }


        /*
         * Permanent learner number is intentionally globally unique
         * according to V034 and must NOT be tenant-scoped.
         */
        if (repository.existsByPermanentLearnerNumber(
                permanentLearnerNumber
        )) {
            throw new IllegalArgumentException(
                    "Permanent learner number already exists"
            );
        }

        if (command.admissionApplicationId() != null) {

            admissionApplications
                    .findByTenantIdAndId(
                            tenantId,
                            command.admissionApplicationId()
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Admission application not found for tenant"
                            )
                    );

            repository
                    .findByTenantIdAndAdmissionApplicationId(
                            tenantId,
                            command.admissionApplicationId()
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Admission application already has a student profile"
                        );
                    });
        }
        Student student =
                new Student(
                        command.admissionApplicationId(),
                        studentNumber,
                        permanentLearnerNumber,
                        command.firstName(),
                        command.middleName(),
                        command.lastName(),
                        command.preferredName(),
                        command.dateOfBirth(),
                        command.gender(),
                        command.nationalityCode(),
                        command.countryOfBirthCode(),
                        command.primaryLanguage(),
                        command.religion(),
                        command.email(),
                        command.phoneNumber(),
                        command.physicalAddress(),
                        command.eiamUserId(),
                        command.edsStudentFileId(),
                        command.admissionDate(),
                        command.firstEnrollmentDate(),
                        command.expectedCompletionDate(),
                        command.completionDate()
                );

        student.setTenantId(
                tenantId
        );

        return repository.save(
                student
        );
    }

    @Transactional(readOnly = true)
    public List<Student> findActiveStudents(
            UUID tenantId
    ) {

        return repository.findByTenantIdAndStatus(
                tenantId,
                EntityStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public Student get(
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
                                "Student not found"
                        )
                );
    }
}
