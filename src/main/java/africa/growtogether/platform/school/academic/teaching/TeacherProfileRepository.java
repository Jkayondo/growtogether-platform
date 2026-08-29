package africa.growtogether.platform.school.academic.teaching;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherProfileRepository
        extends JpaRepository<TeacherProfile, UUID> {

    Optional<TeacherProfile> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<TeacherProfile> findByTenantIdAndTeacherNumber(
            UUID tenantId,
            String teacherNumber
    );

    Optional<TeacherProfile> findByTenantIdAndWorkforceMemberId(
            UUID tenantId,
            UUID workforceMemberId
    );

    Optional<TeacherProfile> findByTenantIdAndTeacherRegistrationNumber(
            UUID tenantId,
            String teacherRegistrationNumber
    );

    List<TeacherProfile> findByTenantIdAndTeacherCategory(
            UUID tenantId,
            String teacherCategory
    );

    List<TeacherProfile> findByTenantIdAndTeachingStatus(
            UUID tenantId,
            String teachingStatus
    );
}
