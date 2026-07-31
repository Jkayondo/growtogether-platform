package africa.growtogether.platform.school.parent.notification;


import africa.growtogether.platform.school.parent.notification.provider.NotificationDeliveryRequest;
import africa.growtogether.platform.school.parent.notification.provider.NotificationDeliveryResponse;
import africa.growtogether.platform.school.parent.notification.provider.ParentNotificationProviderService;
import africa.growtogether.platform.school.parent.notification.dto.ParentNotificationResponse;
import africa.growtogether.platform.school.parent.notification.dto.SendParentNotificationRequest;
import africa.growtogether.platform.school.parent.notification.provider.*;

import org.springframework.stereotype.Service;


@Service
public class ParentNotificationWorkflowService {


    private final ParentNotificationProviderService providerService;


    public ParentNotificationWorkflowService(
            ParentNotificationProviderService providerService
    ) {

        this.providerService = providerService;
    }


    public ParentNotificationResponse send(
            SendParentNotificationRequest request
    ) {


        NotificationDeliveryResponse response =
                providerService.deliver(
                        new NotificationDeliveryRequest(
                                request.parentId(),
                                request.destination(),
                                request.message(),
                                request.channel()
                        )
                );


        return new ParentNotificationResponse(
                null,
                response.successful(),
                response.providerReference(),
                response.message()
        );
    }
}
