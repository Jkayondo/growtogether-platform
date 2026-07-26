package africa.growtogether.platform.school.student;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByPermanentLearnerNumber(String permanentLearnerNumber);

    Optional<Student> findById(UUID id);

}
