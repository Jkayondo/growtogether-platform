package africa.growtogether.platform.school.admission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import africa.growtogether.platform.ens.NotificationChannel;

import org.junit.jupiter.api.Test;

class AdmissionGuardianActivationNotificationPolicyTest {

    @Test
    void emailIdentityAlwaysUsesEmail() {
        AdmissionGuardianActivationNotificationPolicy policy =
                new AdmissionGuardianActivationNotificationPolicy(
                        new AdmissionGuardianActivationNotificationProperties(
                                NotificationChannel.WHATSAPP
                        )
                );

        assertEquals(
                NotificationChannel.EMAIL,
                policy.channelFor(
                        AdmissionGuardianContactIdentityType.EMAIL
                )
        );
    }

    @Test
    void phoneIdentityDefaultsToSmsWhenUnset() {
        AdmissionGuardianActivationNotificationPolicy policy =
                new AdmissionGuardianActivationNotificationPolicy(
                        new AdmissionGuardianActivationNotificationProperties(
                                null
                        )
                );

        assertEquals(
                NotificationChannel.SMS,
                policy.channelFor(
                        AdmissionGuardianContactIdentityType.PHONE
                )
        );
    }

    @Test
    void phoneIdentityMayUseWhatsappWhenConfigured() {
        AdmissionGuardianActivationNotificationPolicy policy =
                new AdmissionGuardianActivationNotificationPolicy(
                        new AdmissionGuardianActivationNotificationProperties(
                                NotificationChannel.WHATSAPP
                        )
                );

        assertEquals(
                NotificationChannel.WHATSAPP,
                policy.channelFor(
                        AdmissionGuardianContactIdentityType.PHONE
                )
        );
    }

    @Test
    void nonPhoneExternalChannelIsRejected() {
        AdmissionGuardianActivationNotificationPolicy policy =
                new AdmissionGuardianActivationNotificationPolicy(
                        new AdmissionGuardianActivationNotificationProperties(
                                NotificationChannel.PUSH
                        )
                );

        assertThrows(
                IllegalStateException.class,
                () -> policy.channelFor(
                        AdmissionGuardianContactIdentityType.PHONE
                )
        );
    }
}
