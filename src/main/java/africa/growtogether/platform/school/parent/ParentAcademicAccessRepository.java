package africa.growtogether.platform.school.parent;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentAcademicAccessRepository
        extends JpaRepository<ParentAcademicAccess, UUID> {


    List<ParentAcademicAccess>
    findByParentId(
            UUID parentId
    );


    List<ParentAcademicAccess>
    findByLearnerId(
            UUID learnerId
    );


    List<ParentAcademicAccess>
    findByParentIdAndAccessStatus(
            UUID parentId,
            ParentAcademicAccessStatus status
    );


    List<ParentAcademicAccess>
    findByTenantId(
            UUID tenantId
    );
}
