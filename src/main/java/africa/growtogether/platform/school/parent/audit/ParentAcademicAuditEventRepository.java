package africa.growtogether.platform.school.parent.audit;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentAcademicAuditEventRepository
        extends JpaRepository<ParentAcademicAuditEvent, UUID> {


    List<ParentAcademicAuditEvent>
    findByParentId(
            UUID parentId
    );


    List<ParentAcademicAuditEvent>
    findByLearnerId(
            UUID learnerId
    );


    List<ParentAcademicAuditEvent>
    findByTenantId(
            UUID tenantId
    );
}
