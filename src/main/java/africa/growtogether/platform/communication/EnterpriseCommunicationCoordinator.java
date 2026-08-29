package africa.growtogether.platform.communication;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.ConnectIntent;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.DeliveryCommand;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.DeliveryResult;
import africa.growtogether.platform.communication.EnterpriseCommunicationDtos.NotificationIntent;

import africa.growtogether.platform.connect.ConnectMessage;
import africa.growtogether.platform.connect.integration.ConnectInternalMessageGateway;

import africa.growtogether.platform.ens.NotificationDtos;
import africa.growtogether.platform.ens.NotificationService;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnterpriseCommunicationCoordinator {

    private final ConnectInternalMessageGateway connect;
    private final NotificationService notifications;

    public EnterpriseCommunicationCoordinator(
            ConnectInternalMessageGateway connect,
            NotificationService notifications
    ) {
        this.connect =
                Objects.requireNonNull(
                        connect,
                        "connect must not be null"
                );

        this.notifications =
                Objects.requireNonNull(
                        notifications,
                        "notifications must not be null"
                );
    }

    /**
     * Coordinates durable communication intent across GT's internal
     * collaboration channel and ENS.
     *
     * This method creates durable records only:
     *
     * - GT Connect SYSTEM message, when requested;
     * - ENS queued notification request, when requested.
     *
     * External provider traffic remains ENS dispatcher responsibility.
     *
     * Security-sensitive notification flows that require special payload
     * handling remain in their specialist services and are not automatically
     * routed through this coordinator.
     */
    @Transactional
    public DeliveryResult deliver(
            DeliveryCommand command
    ) {

        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        UUID tenantId =
                Objects.requireNonNull(
                        command.tenantId(),
                        "tenantId must not be null"
                );

        String sourceService =
                requiredText(
                        command.sourceService(),
                        "sourceService"
                );

        if (
                command.connect() == null
                        && command.notification() == null
        ) {
            throw new IllegalArgumentException(
                    "At least one communication delivery intent is required"
            );
        }

        validateConnectIntent(
                command.connect()
        );

        validateNotificationIntent(
                command.notification()
        );

        RequestContext previous =
                RequestContextHolder.current()
                        .orElse(null);

        requireCompatibleTenant(
                previous,
                tenantId
        );

        String correlationId =
                effectiveCorrelationId(
                        command.correlationId(),
                        previous
                );

        RequestContextHolder.set(
                new RequestContext(
                        correlationId,
                        tenantId.toString()
                )
        );

        try {

            ConnectMessage connectMessage =
                    deliverConnect(
                            tenantId,
                            sourceService,
                            command.sourceReference(),
                            correlationId,
                            command.connect()
                    );

            NotificationDtos.View notification =
                    deliverNotification(
                            tenantId,
                            sourceService,
                            command.sourceReference(),
                            command.notification()
                    );

            return new DeliveryResult(
                    connectMessage,
                    notification,
                    correlationId
            );

        } finally {

            restore(
                    previous
            );
        }
    }

    private ConnectMessage deliverConnect(
            UUID tenantId,
            String sourceService,
            String sourceReference,
            String correlationId,
            ConnectIntent intent
    ) {

        if (intent == null) {
            return null;
        }

        return connect.sendSystemMessage(
                tenantId,
                intent.spaceId(),
                intent.body(),
                sourceService,
                sourceReference,
                correlationId
        );
    }

    private NotificationDtos.View deliverNotification(
            UUID tenantId,
            String sourceService,
            String sourceReference,
            NotificationIntent intent
    ) {

        if (intent == null) {
            return null;
        }

        return notifications.sendForTenant(
                tenantId,
                new NotificationDtos.SendCommand(
                        intent.definitionCode(),
                        intent.recipient(),
                        intent.channel(),
                        intent.priority(),
                        intent.subject(),
                        intent.body(),
                        sourceService,
                        sourceReference
                )
        );
    }

    private static void validateConnectIntent(
            ConnectIntent intent
    ) {

        if (intent == null) {
            return;
        }

        Objects.requireNonNull(
                intent.spaceId(),
                "Connect spaceId must not be null"
        );

        requiredText(
                intent.body(),
                "Connect body"
        );
    }

    private static void validateNotificationIntent(
            NotificationIntent intent
    ) {

        if (intent == null) {
            return;
        }

        requiredText(
                intent.definitionCode(),
                "notification definitionCode"
        );

        requiredText(
                intent.recipient(),
                "notification recipient"
        );

        Objects.requireNonNull(
                intent.channel(),
                "notification channel must not be null"
        );

        requiredText(
                intent.body(),
                "notification body"
        );
    }

    private static String requiredText(
            String value,
            String name
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    name + " is required"
            );
        }

        return value.trim();
    }

    private static void requireCompatibleTenant(
            RequestContext previous,
            UUID tenantId
    ) {

        if (
                previous == null
                        || previous.tenantId() == null
                        || previous.tenantId().isBlank()
        ) {
            return;
        }

        UUID contextualTenant;

        try {
            contextualTenant =
                    UUID.fromString(
                            previous.tenantId()
                    );

        } catch (IllegalArgumentException exception) {
            throw new TenantScopeViolationException(
                    "Active tenant context is invalid."
            );
        }

        if (
                !tenantId.equals(
                        contextualTenant
                )
        ) {
            throw new TenantScopeViolationException(
                    "Enterprise communication cannot cross tenant boundaries."
            );
        }
    }

    private static String effectiveCorrelationId(
            String requested,
            RequestContext previous
    ) {

        if (
                requested != null
                        && !requested.isBlank()
        ) {
            return requested.trim();
        }

        if (
                previous != null
                        && previous.correlationId() != null
                        && !previous.correlationId().isBlank()
        ) {
            return previous.correlationId().trim();
        }

        return "gt-communication-"
                + UUID.randomUUID();
    }

    private static void restore(
            RequestContext previous
    ) {

        if (previous == null) {
            RequestContextHolder.clear();

        } else {
            RequestContextHolder.set(
                    previous
            );
        }
    }
}
