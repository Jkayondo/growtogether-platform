package africa.growtogether.platform.school.enrollment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentEnrollmentRepository
        extends JpaRepository<StudentEnrollment, UUID> {


    Optional<StudentEnrollment> 
    findFirstByStudentIdAndEnrollmentStatusOrderByEnrollmentDateDesc(
            UUID studentId,
            String enrollmentStatus
    );

}
