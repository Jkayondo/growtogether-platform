package africa.growtogether.platform.school.parent.notification.delivery;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentNotificationDeliveryService {


    private final ParentNotificationDeliveryRepository repository;


    public ParentNotificationDeliveryService(
            ParentNotificationDeliveryRepository repository
    ) {

        this.repository = repository;
    }


    public ParentNotificationDelivery createDelivery(
            UUID tenantId,
            UUID notificationId,
            String channel
    ) {

        ParentNotificationDelivery delivery =
                new ParentNotificationDelivery(
                        tenantId,
                        notificationId,
                        channel
                );


        return repository.save(delivery);
    }


    public ParentNotificationDelivery markSent(
            ParentNotificationDelivery delivery
    ) {

        delivery.markSent();

        return repository.save(delivery);
    }


    public ParentNotificationDelivery markFailed(
            ParentNotificationDelivery delivery
    ) {

        delivery.markFailed();

        return repository.save(delivery);
    }
}
