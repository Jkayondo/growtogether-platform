package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.ens.NotificationChannel;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "gt.school.admission.parent-activation"
)
public record AdmissionGuardianActivationNotificationProperties(
        NotificationChannel phoneChannel
) {
}
