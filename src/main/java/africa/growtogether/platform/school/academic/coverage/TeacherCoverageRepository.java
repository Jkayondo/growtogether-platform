package africa.growtogether.platform.school.academic.coverage;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface TeacherCoverageRepository
        extends JpaRepository<TeacherCoverage, UUID> {


    List<TeacherCoverage> findByTenantIdAndTeacherProfileId(
            UUID tenantId,
            UUID teacherProfileId
    );


    List<TeacherCoverage> findByTenantIdAndTeachingAssignmentId(
            UUID tenantId,
            UUID teachingAssignmentId
    );


    List<TeacherCoverage> findByTenantIdAndAcademicTermId(
            UUID tenantId,
            UUID academicTermId
    );


    List<TeacherCoverage> findByTenantIdAndCoverageStatus(
            UUID tenantId,
            String coverageStatus
    );


    java.util.Optional<TeacherCoverage> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


}
