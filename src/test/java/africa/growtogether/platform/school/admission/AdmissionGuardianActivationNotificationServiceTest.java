package africa.growtogether.platform.school.admission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationDtos.View;
import africa.growtogether.platform.ens.NotificationPriority;
import africa.growtogether.platform.ens.NotificationSecurePayloadService;
import africa.growtogether.platform.ens.NotificationService;
import africa.growtogether.platform.ens.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

class AdmissionGuardianActivationNotificationServiceTest {

    private AdmissionGuardianAccountProvisioningService
            provisioningService;

    private AdmissionGuardianActivationNotificationPolicy
            channelPolicy;

    private NotificationService notifications;

    private NotificationSecurePayloadService securePayloads;

    private AdmissionGuardianActivationNotificationService service;

    private UUID tenantId;

    private UUID admissionGuardianId;

    @BeforeEach
    void setUp() {
        provisioningService =
                mock(
                        AdmissionGuardianAccountProvisioningService.class
                );

        channelPolicy =
                mock(
                        AdmissionGuardianActivationNotificationPolicy.class
                );

        notifications =
                mock(NotificationService.class);

        securePayloads =
                mock(NotificationSecurePayloadService.class);

        service =
                new AdmissionGuardianActivationNotificationService(
                        provisioningService,
                        channelPolicy,
                        notifications,
                        securePayloads
                );

        tenantId =
                UUID.randomUUID();

        admissionGuardianId =
                UUID.randomUUID();

        RequestContextHolder.set(
                new RequestContext(
                        "b8-test-correlation",
                        tenantId.toString()
                )
        );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void successfulNotificationKeepsTokenOutOfOrdinaryEnsBody() {
        String token =
                "VERY-SECRET-ONE-TIME-TOKEN";

        Instant expiresAt =
                Instant.now()
                        .plusSeconds(900);

        AdmissionGuardianAccountProvisioningResult provisioning =
                provisioning(
                        token,
                        expiresAt
                );

        when(
                provisioningService.provisionParentAccount(
                        admissionGuardianId
                )
        ).thenReturn(provisioning);

        when(
                channelPolicy.channelFor(
                        AdmissionGuardianContactIdentityType.EMAIL
                )
        ).thenReturn(
                NotificationChannel.EMAIL
        );

        UUID notificationId =
                UUID.randomUUID();

        when(
                notifications.sendForTenant(
                        eq(tenantId),
                        any(SendCommand.class)
                )
        ).thenReturn(
                notificationView(
                        notificationId,
                        tenantId,
                        NotificationChannel.EMAIL
                )
        );

        AdmissionGuardianActivationNotificationResult result =
                service.provisionAndNotify(
                        admissionGuardianId
                );

        ArgumentCaptor<SendCommand> sendCaptor =
                ArgumentCaptor.forClass(
                        SendCommand.class
                );

        verify(notifications).sendForTenant(
                eq(tenantId),
                sendCaptor.capture()
        );

        SendCommand persistedCommand =
                sendCaptor.getValue();

        assertThat(
                persistedCommand.body()
        ).doesNotContain(token);

        assertThat(
                persistedCommand.body()
        ).contains(
                "Secure parent account activation instructions"
        );

        assertThat(
                persistedCommand.recipient()
        ).isEqualTo(
                provisioning.contactIdentity()
        );

        assertThat(
                persistedCommand.channel()
        ).isEqualTo(
                NotificationChannel.EMAIL
        );

        ArgumentCaptor<String> secureBodyCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(securePayloads).attach(
                eq(tenantId),
                eq(notificationId),
                secureBodyCaptor.capture(),
                eq(expiresAt)
        );

        String secureBody =
                secureBodyCaptor.getValue();

        assertThat(
                secureBody
        ).contains(token);

        assertThat(
                secureBody
        ).contains(
                "create your own username and password"
        );

        assertThat(
                result.notificationRequestId()
        ).isEqualTo(notificationId);

        assertThat(
                result.channel()
        ).isEqualTo(NotificationChannel.EMAIL);

        assertThat(
                result.expiresAt()
        ).isEqualTo(expiresAt);

        assertThat(
                result.toString()
        ).doesNotContain(token);
    }

    @Test
    void mismatchedEnsTenantIsRejectedBeforeSecureAttachment() {
        String token =
                "TENANT-MISMATCH-SECRET";

        Instant expiresAt =
                Instant.now()
                        .plusSeconds(900);

        AdmissionGuardianAccountProvisioningResult provisioning =
                provisioning(
                        token,
                        expiresAt
                );

        when(
                provisioningService.provisionParentAccount(
                        admissionGuardianId
                )
        ).thenReturn(provisioning);

        when(
                channelPolicy.channelFor(
                        AdmissionGuardianContactIdentityType.EMAIL
                )
        ).thenReturn(
                NotificationChannel.EMAIL
        );

        UUID wrongTenant =
                UUID.randomUUID();

        when(
                notifications.sendForTenant(
                        eq(tenantId),
                        any(SendCommand.class)
                )
        ).thenReturn(
                notificationView(
                        UUID.randomUUID(),
                        wrongTenant,
                        NotificationChannel.EMAIL
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.provisionAndNotify(
                        admissionGuardianId
                )
        );

        verify(
                securePayloads,
                never()
        ).attach(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(Instant.class)
        );
    }

    @Test
    void mismatchedEnsChannelIsRejectedBeforeSecureAttachment() {
        String token =
                "CHANNEL-MISMATCH-SECRET";

        Instant expiresAt =
                Instant.now()
                        .plusSeconds(900);

        AdmissionGuardianAccountProvisioningResult provisioning =
                provisioning(
                        token,
                        expiresAt
                );

        when(
                provisioningService.provisionParentAccount(
                        admissionGuardianId
                )
        ).thenReturn(provisioning);

        when(
                channelPolicy.channelFor(
                        AdmissionGuardianContactIdentityType.EMAIL
                )
        ).thenReturn(
                NotificationChannel.EMAIL
        );

        when(
                notifications.sendForTenant(
                        eq(tenantId),
                        any(SendCommand.class)
                )
        ).thenReturn(
                notificationView(
                        UUID.randomUUID(),
                        tenantId,
                        NotificationChannel.SMS
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.provisionAndNotify(
                        admissionGuardianId
                )
        );

        verify(
                securePayloads,
                never()
        ).attach(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(Instant.class)
        );
    }

    @Test
    void expiredInvitationIsRejectedBeforeEnsNotificationCreation() {
        AdmissionGuardianAccountProvisioningResult provisioning =
                provisioning(
                        "EXPIRED-SECRET",
                        Instant.now()
                                .minusSeconds(1)
                );

        when(
                provisioningService.provisionParentAccount(
                        admissionGuardianId
                )
        ).thenReturn(provisioning);

        assertThrows(
                IllegalStateException.class,
                () -> service.provisionAndNotify(
                        admissionGuardianId
                )
        );

        verify(
                notifications,
                never()
        ).sendForTenant(
                any(UUID.class),
                any(SendCommand.class)
        );

        verify(
                securePayloads,
                never()
        ).attach(
                any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(Instant.class)
        );
    }

    private AdmissionGuardianAccountProvisioningResult provisioning(
            String token,
            Instant expiresAt
    ) {
        return new AdmissionGuardianAccountProvisioningResult(
                UUID.randomUUID(),
                admissionGuardianId,
                UUID.randomUUID(),
                AdmissionGuardianContactIdentityType.EMAIL,
                "parent@example.com",
                expiresAt,
                token
        );
    }

    private static View notificationView(
            UUID notificationId,
            UUID tenantId,
            NotificationChannel channel
    ) {
        return new View(
                notificationId,
                tenantId,
                "GT-SCHOOL-PARENT-ACTIVATION",
                "parent@example.com",
                channel,
                NotificationPriority.NORMAL,
                NotificationStatus.QUEUED,
                "GrowTogether parent account activation",
                "GT-SCHOOL",
                "provisioning-reference",
                0,
                null,
                null,
                null
        );
    }
}
