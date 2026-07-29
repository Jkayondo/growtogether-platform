package africa.growtogether.platform.school.parent.notification.provider;


public interface NotificationProvider {


    NotificationDeliveryResponse send(
            NotificationDeliveryRequest request
    );

}
