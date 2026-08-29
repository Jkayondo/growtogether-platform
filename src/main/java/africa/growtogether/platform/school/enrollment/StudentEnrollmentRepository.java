package africa.growtogether.platform.school.enrollment;

import africa.growtogether.platform.common.persistence.EntityStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentEnrollmentRepository
        extends JpaRepository<StudentEnrollment, UUID> {

    Optional<StudentEnrollment> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<StudentEnrollment> findByTenantIdAndEnrollmentNumber(
            UUID tenantId,
            String enrollmentNumber
    );

    boolean existsByTenantIdAndEnrollmentNumber(
            UUID tenantId,
            String enrollmentNumber
    );

    boolean existsByTenantIdAndStudentIdAndAcademicYearIdAndAcademicTermId(
            UUID tenantId,
            UUID studentId,
            UUID academicYearId,
            UUID academicTermId
    );

    Optional<StudentEnrollment>
    findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
            UUID tenantId,
            UUID studentId,
            String enrollmentStatus,
            EntityStatus status
    );

    List<StudentEnrollment>
    findByTenantIdAndStudentIdOrderByEnrollmentDateDesc(
            UUID tenantId,
            UUID studentId
    );

    List<StudentEnrollment>
    findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );

    List<StudentEnrollment>
    findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );

    List<StudentEnrollment>
    findByTenantIdAndClassGradeId(
            UUID tenantId,
            UUID classGradeId
    );

    List<StudentEnrollment>
    findByTenantIdAndStreamId(
            UUID tenantId,
            UUID streamId
    );

    List<StudentEnrollment>
    findByTenantIdAndEnrollmentStatus(
            UUID tenantId,
            String enrollmentStatus
    );

    /*
     * Legacy compatibility query used by Learner360Service.
     *
     * It is intentionally retained only until Learner360 is moved
     * to the tenant-scoped query above.
     */
    Optional<StudentEnrollment>
    findFirstByStudentIdAndEnrollmentStatusOrderByEnrollmentDateDesc(
            UUID studentId,
            String enrollmentStatus
    );
}
