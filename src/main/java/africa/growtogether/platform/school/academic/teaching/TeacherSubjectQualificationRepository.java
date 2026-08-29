package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherSubjectQualificationRepository
        extends JpaRepository<TeacherSubjectQualification, UUID> {

    Optional<TeacherSubjectQualification> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<TeacherSubjectQualification>
    findByTenantIdAndTeacherProfileIdAndSubjectId(
            UUID tenantId,
            UUID teacherProfileId,
            UUID subjectId
    );

    List<TeacherSubjectQualification> findByTenantIdAndTeacherProfileId(
            UUID tenantId,
            UUID teacherProfileId
    );

    List<TeacherSubjectQualification> findByTenantIdAndSubjectId(
            UUID tenantId,
            UUID subjectId
    );

    List<TeacherSubjectQualification>
    findByTenantIdAndVerificationStatus(
            UUID tenantId,
            String verificationStatus
    );

    Optional<TeacherSubjectQualification>
    findByTenantIdAndTeacherProfileIdAndPrimarySubjectTrueAndStatus(
            UUID tenantId,
            UUID teacherProfileId,
            EntityStatus status
    );
}
