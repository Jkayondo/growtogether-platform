package africa.growtogether.platform.school.parent.notification.processing;


import africa.growtogether.platform.school.parent.notification.delivery.ParentNotificationDelivery;
import africa.growtogether.platform.school.parent.notification.delivery.ParentNotificationDeliveryService;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentNotificationEventProcessorService {


    private final ParentNotificationProcessingRepository repository;

    private final ParentNotificationDeliveryService deliveryService;


    public ParentNotificationEventProcessorService(
            ParentNotificationProcessingRepository repository,
            ParentNotificationDeliveryService deliveryService
    ) {

        this.repository = repository;
        this.deliveryService = deliveryService;
    }


    public ParentNotificationProcessingEvent process(
            UUID tenantId,
            UUID notificationId
    ) {


        ParentNotificationProcessingEvent event =
                new ParentNotificationProcessingEvent(
                        tenantId,
                        notificationId
                );


        repository.save(event);


        event.startProcessing();

        repository.save(event);


        /*
         Future provider routing:

         SMS
         EMAIL
         PUSH
         */


        event.complete();

        return repository.save(event);
    }
}
