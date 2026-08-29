package africa.growtogether.platform.school.student;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository
        extends JpaRepository<Student, UUID> {

    boolean existsByTenantIdAndStudentNumber(
            UUID tenantId,
            String studentNumber
    );

    boolean existsByPermanentLearnerNumber(
            String permanentLearnerNumber
    );

    Optional<Student> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<Student> findByTenantIdAndAdmissionApplicationId(
            UUID tenantId,
            UUID admissionApplicationId
    );
}
