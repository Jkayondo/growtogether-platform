package africa.growtogether.platform.school.parent.notification.provider;


import java.util.UUID;


public record NotificationDeliveryRequest(

        UUID parentId,

        String destination,

        String message,

        NotificationChannel channel

) {
}
