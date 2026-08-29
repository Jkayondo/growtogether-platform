package africa.growtogether.platform.school.academic.teaching;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeachingAssignmentRepository
        extends JpaRepository<TeachingAssignment, UUID> {

    Optional<TeachingAssignment> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<TeachingAssignment> findByTenantIdAndAssignmentReference(
            UUID tenantId,
            String assignmentReference
    );

    List<TeachingAssignment> findByTenantIdAndTeacherProfileId(
            UUID tenantId,
            UUID teacherProfileId
    );

    List<TeachingAssignment> findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );

    List<TeachingAssignment> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );

    List<TeachingAssignment> findByTenantIdAndClassGradeId(
            UUID tenantId,
            UUID classGradeId
    );

    List<TeachingAssignment> findByTenantIdAndSubjectId(
            UUID tenantId,
            UUID subjectId
    );

    List<TeachingAssignment> findByTenantIdAndAssignmentStatus(
            UUID tenantId,
            String assignmentStatus
    );

    boolean existsByTenantIdAndTeacherProfileIdAndAcademicYearIdAndAcademicTermIdAndClassGradeIdAndStreamIdAndSubjectId(
            UUID tenantId,
            UUID teacherProfileId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            UUID streamId,
            UUID subjectId
    );

}
