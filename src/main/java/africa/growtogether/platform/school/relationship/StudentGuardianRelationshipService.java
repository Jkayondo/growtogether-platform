package africa.growtogether.platform.school.relationship;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class StudentGuardianRelationshipService {


    private final StudentGuardianRelationshipRepository repository;


    public StudentGuardianRelationshipService(
            StudentGuardianRelationshipRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public StudentGuardianRelationship create(
            CreateStudentGuardianRelationshipCommand command
    ) {


        if (repository.existsByStudentIdAndGuardianId(
                command.studentId(),
                command.guardianId()
        )) {

            throw new IllegalArgumentException(
                    "Student guardian relationship already exists"
            );
        }


        StudentGuardianRelationship relationship =
                new StudentGuardianRelationship(
                        command.studentId(),
                        command.guardianId(),
                        command.relationshipType()
                );


        return repository.save(relationship);
    }



    @Transactional(readOnly = true)
    public StudentGuardianRelationship get(
            UUID id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student guardian relationship not found"
                        )
                );
    }
}
