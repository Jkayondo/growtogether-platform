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
            CreateStudentCommand command
    ) {

        if (repository.existsByStudentNumber(command.studentNumber())) {
            throw new IllegalArgumentException(
                    "Student number already exists"
            );
        }

        if (repository.existsByPermanentLearnerNumber(
                command.permanentLearnerNumber()
        )) {
            throw new IllegalArgumentException(
                    "Permanent learner number already exists"
            );
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

        return repository.save(student);
    }

    @Transactional(readOnly = true)
    public Student get(UUID id) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student not found"
                        )
                );
    }
}
