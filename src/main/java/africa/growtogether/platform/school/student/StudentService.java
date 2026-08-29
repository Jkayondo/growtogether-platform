package africa.growtogether.platform.school.student;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(
            StudentRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public Student create(
            UUID tenantId,
            CreateStudentCommand command
    ) {

        if (repository.existsByTenantIdAndStudentNumber(
                tenantId,
                command.studentNumber()
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
                command.permanentLearnerNumber()
        )) {
            throw new IllegalArgumentException(
                    "Permanent learner number already exists"
            );
        }

        if (command.admissionApplicationId() != null) {

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
                        command.studentNumber(),
                        command.permanentLearnerNumber(),
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
