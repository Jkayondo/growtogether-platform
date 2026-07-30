package africa.growtogether.platform.school.parent.notification.processing;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ParentNotificationProcessingRepository
        extends JpaRepository<ParentNotificationProcessingEvent, UUID> {


    List<ParentNotificationProcessingEvent>
    findByNotificationId(
            UUID notificationId
    );


    List<ParentNotificationProcessingEvent>
    findByProcessingStatus(
            ParentNotificationProcessingStatus status
    );
}
