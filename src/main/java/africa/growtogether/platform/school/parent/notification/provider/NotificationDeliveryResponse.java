package africa.growtogether.platform.school.parent.notification.provider;


public record NotificationDeliveryResponse(

        boolean successful,

        String providerReference,

        String message

) {
}
