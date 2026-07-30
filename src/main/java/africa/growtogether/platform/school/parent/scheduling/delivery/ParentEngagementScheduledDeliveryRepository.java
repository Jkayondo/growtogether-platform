package africa.growtogether.platform.school.parent.scheduling.delivery;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ParentEngagementScheduledDeliveryRepository
        extends JpaRepository<ParentEngagementScheduledDelivery, UUID> {
}
