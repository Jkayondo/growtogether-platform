package africa.growtogether.platform.school.parent.notification.dto;


import africa.growtogether.platform.school.parent.notification.provider.NotificationChannel;

import java.util.UUID;


public record SendParentNotificationRequest(

        UUID parentId,

        String destination,

        String message,

        NotificationChannel channel

) {
}
