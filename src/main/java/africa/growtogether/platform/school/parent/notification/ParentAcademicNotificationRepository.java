package africa.growtogether.platform.school.parent.notification;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentAcademicNotificationRepository
        extends JpaRepository<ParentAcademicNotification, UUID> {


    List<ParentAcademicNotification>
    findByParentId(
            UUID parentId
    );


    List<ParentAcademicNotification>
    findByLearnerId(
            UUID learnerId
    );


    List<ParentAcademicNotification>
    findByTenantId(
            UUID tenantId
    );
}
