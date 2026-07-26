package africa.growtogether.platform.school.relationship;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentGuardianRelationshipRepository
        extends JpaRepository<StudentGuardianRelationship, UUID> {


    boolean existsByStudentIdAndGuardianId(
            UUID studentId,
            UUID guardianId
    );


    List<StudentGuardianRelationship> findByStudentId(
            UUID studentId
    );

}
