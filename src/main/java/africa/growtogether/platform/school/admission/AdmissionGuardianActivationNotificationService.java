package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import africa.growtogether.platform.ens.NotificationDtos.View;
import africa.growtogether.platform.ens.NotificationPriority;
import africa.growtogether.platform.ens.NotificationSecurePayloadService;
import africa.growtogether.platform.ens.NotificationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AdmissionGuardianActivationNotificationService {

    private static final String DEFINITION_CODE =
            "GT-SCHOOL-PARENT-ACTIVATION";

    private static final String SOURCE_SERVICE =
            "GT-SCHOOL";

    private static final String SUBJECT =
            "GrowTogether parent account activation";

    /*
     * This body is intentionally safe for ordinary ENS persistence.
     * It must never contain the EIAM invitation token.
     */
    private static final String SAFE_PERSISTED_BODY =
            "Secure parent account activation instructions "
                    + "are ready for delivery.";

    private final AdmissionGuardianAccountProvisioningService
            provisioningService;

    private final AdmissionGuardianActivationNotificationPolicy
            channelPolicy;

    private final NotificationService notifications;

    private final NotificationSecurePayloadService securePayloads;

    public AdmissionGuardianActivationNotificationService(
            AdmissionGuardianAccountProvisioningService provisioningService,
            AdmissionGuardianActivationNotificationPolicy channelPolicy,
            NotificationService notifications,
            NotificationSecurePayloadService securePayloads
    ) {
        this.provisioningService =
                Objects.requireNonNull(
                        provisioningService,
                        "provisioningService must not be null"
                );

        this.channelPolicy =
                Objects.requireNonNull(
                        channelPolicy,
                        "channelPolicy must not be null"
                );

        this.notifications =
                Objects.requireNonNull(
                        notifications,
                        "notifications must not be null"
                );

        this.securePayloads =
                Objects.requireNonNull(
                        securePayloads,
                        "securePayloads must not be null"
                );
    }

    /*
     * One transaction intentionally spans:
     *
     * 1. EIAM invitation + V156 admission provisioning;
     * 2. ordinary ENS notification creation;
     * 3. encrypted V159 secure payload attachment.
     *
     * If secure attachment fails, the complete operation rolls back.
     */
    @Transactional
    public AdmissionGuardianActivationNotificationResult
    provisionAndNotify(
            UUID admissionGuardianId
    ) {
        if (admissionGuardianId == null) {
            throw new IllegalArgumentException(
                    "admissionGuardianId must not be null"
            );
        }

        UUID tenantId =
                activeTenant();

        AdmissionGuardianAccountProvisioningResult provisioning =
                provisioningService.provisionParentAccount(
                        admissionGuardianId
                );

        validateProvisioningEvidence(
                admissionGuardianId,
                provisioning
        );

        NotificationChannel channel =
                channelPolicy.channelFor(
                        provisioning.contactIdentityType()
                );

        View notification =
                notifications.sendForTenant(
                        tenantId,
                        new SendCommand(
                                DEFINITION_CODE,
                                provisioning.contactIdentity(),
                                channel,
                                NotificationPriority.NORMAL,
                                SUBJECT,
                                SAFE_PERSISTED_BODY,
                                SOURCE_SERVICE,
                                provisioning.provisioningId()
                                        .toString()
                        )
                );

        validateNotificationEvidence(
                tenantId,
                channel,
                notification
        );

        /*
         * The final provider body exists in plaintext only in memory here
         * and later inside the dispatcher immediately before provider traffic.
         *
         * It is encrypted by NotificationSecurePayloadService before
         * persistence. Its lifetime is bounded by the EIAM invitation expiry.
         */
        securePayloads.attach(
                tenantId,
                notification.id(),
                secureProviderBody(
                        provisioning.acceptanceToken(),
                        provisioning.expiresAt()
                ),
                provisioning.expiresAt()
        );

        return new AdmissionGuardianActivationNotificationResult(
                provisioning.provisioningId(),
                provisioning.admissionGuardianId(),
                provisioning.invitationId(),
                notification.id(),
                channel,
                provisioning.expiresAt()
        );
    }

    private static void validateProvisioningEvidence(
            UUID expectedAdmissionGuardianId,
            AdmissionGuardianAccountProvisioningResult provisioning
    ) {
        if (provisioning == null) {
            throw new IllegalStateException(
                    "Parent account provisioning returned no evidence"
            );
        }

        if (
                provisioning.provisioningId() == null
                        || provisioning.admissionGuardianId() == null
                        || provisioning.invitationId() == null
                        || provisioning.contactIdentityType() == null
                        || provisioning.contactIdentity() == null
                        || provisioning.contactIdentity().isBlank()
                        || provisioning.expiresAt() == null
                        || provisioning.acceptanceToken() == null
                        || provisioning.acceptanceToken().isBlank()
        ) {
            throw new IllegalStateException(
                    "Parent account provisioning evidence is incomplete"
            );
        }

        if (
                !expectedAdmissionGuardianId.equals(
                        provisioning.admissionGuardianId()
                )
        ) {
            throw new IllegalStateException(
                    "Parent account provisioning evidence "
                            + "does not match the admission guardian"
            );
        }

        if (
                !provisioning.expiresAt()
                        .isAfter(
                                Instant.now()
                        )
        ) {
            throw new IllegalStateException(
                    "Parent account invitation has already expired"
            );
        }
    }

    private static void validateNotificationEvidence(
            UUID tenantId,
            NotificationChannel expectedChannel,
            View notification
    ) {
        if (
                notification == null
                        || notification.id() == null
                        || notification.tenantId() == null
                        || notification.channel() == null
        ) {
            throw new IllegalStateException(
                    "ENS did not return durable notification evidence"
            );
        }

        if (
                !tenantId.equals(
                        notification.tenantId()
                )
        ) {
            throw new IllegalStateException(
                    "ENS notification tenant does not match "
                            + "the active tenant"
            );
        }

        if (
                notification.channel()
                        != expectedChannel
        ) {
            throw new IllegalStateException(
                    "ENS notification channel does not match "
                            + "the activation delivery policy"
            );
        }
    }

    private static String secureProviderBody(
            String acceptanceToken,
            Instant expiresAt
    ) {
        return "Your GrowTogether parent account is ready for activation.\n"
                + "Use this one-time activation token:\n"
                + acceptanceToken
                + "\n\nThis token expires at "
                + expiresAt
                + ".\n"
                + "Open GrowTogether and choose Parent Account Activation.\n"
                + "You will create your own username and password during "
                + "activation.\n"
                + "Do not share this token with anyone.";
    }

    private static UUID activeTenant() {
        return RequestContextHolder
                .current()
                .map(
                        context ->
                                context.tenantId()
                )
                .filter(
                        value ->
                                value != null
                                        && !value.isBlank()
                )
                .map(
                        UUID::fromString
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "An active tenant is required."
                                )
                );
    }
}
