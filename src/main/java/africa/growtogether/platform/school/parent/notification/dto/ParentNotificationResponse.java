package africa.growtogether.platform.school.parent.notification.dto;


import java.util.UUID;


public record ParentNotificationResponse(

        UUID deliveryId,

        boolean successful,

        String providerReference,

        String message

) {
}
