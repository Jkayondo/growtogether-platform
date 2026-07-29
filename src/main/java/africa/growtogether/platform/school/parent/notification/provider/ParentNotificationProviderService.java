package africa.growtogether.platform.school.parent.notification.provider;


import org.springframework.stereotype.Service;


@Service
public class ParentNotificationProviderService {


    private final NotificationProvider provider;


    public ParentNotificationProviderService(
            NotificationProvider provider
    ) {

        this.provider = provider;
    }


    public NotificationDeliveryResponse deliver(
            NotificationDeliveryRequest request
    ) {

        return provider.send(request);
    }
}
