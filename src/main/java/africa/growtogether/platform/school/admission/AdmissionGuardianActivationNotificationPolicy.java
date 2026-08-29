package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.ens.NotificationChannel;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(
        AdmissionGuardianActivationNotificationProperties.class
)
public class AdmissionGuardianActivationNotificationPolicy {

    private final AdmissionGuardianActivationNotificationProperties
            properties;

    public AdmissionGuardianActivationNotificationPolicy(
            AdmissionGuardianActivationNotificationProperties properties
    ) {
        this.properties = properties;
    }

    public NotificationChannel channelFor(
            AdmissionGuardianContactIdentityType identityType
    ) {
        if (identityType == null) {
            throw new IllegalArgumentException(
                    "identityType must not be null"
            );
        }

        if (
                identityType
                        == AdmissionGuardianContactIdentityType.EMAIL
        ) {
            return NotificationChannel.EMAIL;
        }

        NotificationChannel configured =
                properties.phoneChannel();

        if (configured == null) {
            return NotificationChannel.SMS;
        }

        if (
                configured != NotificationChannel.SMS
                        && configured != NotificationChannel.WHATSAPP
        ) {
            throw new IllegalStateException(
                    "Parent activation phone channel must be "
                            + "SMS or WHATSAPP"
            );
        }

        return configured;
    }
}
