package africa.growtogether.platform.school.parent.notification.provider;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class DefaultNotificationProvider
        implements NotificationProvider {


    @Override
    public NotificationDeliveryResponse send(
            NotificationDeliveryRequest request
    ) {

        String providerReference =
                "GT-NOTIFY-" + UUID.randomUUID();


        return new NotificationDeliveryResponse(
                true,
                providerReference,
                "Notification accepted for delivery"
        );
    }
}
