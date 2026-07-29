package africa.growtogether.platform.school.parent.notification.delivery;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentNotificationDeliveryRepository
        extends JpaRepository<ParentNotificationDelivery, UUID> {


    List<ParentNotificationDelivery>
    findByNotificationId(
            UUID notificationId
    );


    List<ParentNotificationDelivery>
    findByDeliveryStatus(
            ParentNotificationDeliveryStatus status
    );


    List<ParentNotificationDelivery>
    findByTenantId(
            UUID tenantId
    );
}
