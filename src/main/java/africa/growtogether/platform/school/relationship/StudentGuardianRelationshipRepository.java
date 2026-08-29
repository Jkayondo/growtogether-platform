package africa.growtogether.platform.school.relationship;

import africa.growtogether.platform.common.persistence.EntityStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGuardianRelationshipRepository
        extends JpaRepository<StudentGuardianRelationship, UUID> {

    /*
     * Legacy compatibility methods.
     */
    boolean existsByStudentIdAndGuardianId(
            UUID studentId,
            UUID guardianId
    );

    List<StudentGuardianRelationship> findByStudentId(
            UUID studentId
    );

    /*
     * Tenant-safe Release 1 methods.
     */
    Optional<StudentGuardianRelationship> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantIdAndStudentIdAndGuardianId(
            UUID tenantId,
            UUID studentId,
            UUID guardianId
    );

    List<StudentGuardianRelationship> findByTenantIdAndStudentId(
            UUID tenantId,
            UUID studentId
    );

    List<StudentGuardianRelationship> findByTenantIdAndGuardianId(
            UUID tenantId,
            UUID guardianId
    );

    Optional<StudentGuardianRelationship>
    findFirstByTenantIdAndStudentIdAndPrimaryGuardianTrueAndRelationshipStatusAndStatus(
            UUID tenantId,
            UUID studentId,
            String relationshipStatus,
            EntityStatus status
    );
}
