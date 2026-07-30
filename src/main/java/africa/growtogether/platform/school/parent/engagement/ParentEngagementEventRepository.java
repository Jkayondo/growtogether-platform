package africa.growtogether.platform.school.parent.engagement;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentEngagementEventRepository
        extends JpaRepository<ParentEngagementEvent, UUID> {


    List<ParentEngagementEvent>
    findByParentId(
            UUID parentId
    );


    List<ParentEngagementEvent>
    findByLearnerId(
            UUID learnerId
    );


    List<ParentEngagementEvent>
    findByTenantId(
            UUID tenantId
    );
}
