package africa.growtogether.platform.connect.integration;

import africa.growtogether.platform.common.persistence.TenantScopeViolationException;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.connect.ConnectMessage;
import africa.growtogether.platform.connect.ConnectMessageRepository;
import africa.growtogether.platform.connect.ConnectSpace;
import africa.growtogether.platform.connect.ConnectSpaceRepository;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectInternalMessageGateway {

    private final ConnectSpaceRepository spaces;
    private final ConnectMessageRepository messages;

    public ConnectInternalMessageGateway(
            ConnectSpaceRepository spaces,
            ConnectMessageRepository messages
    ) {
        this.spaces = spaces;
        this.messages = messages;
    }

    /**
     * Publishes an automated internal GT message into an existing
     * tenant-scoped Connect space.
     *
     * This method deliberately does not impersonate a human user.
     * Automated messages use ConnectMessageType.SYSTEM with:
     *
     * senderUserId   = null
     * sourceService  = originating GT capability
     * sourceReference = originating record/event reference
     *
     * JPA auditing records the technical actor as "system".
     */
    @Transactional
    public ConnectMessage sendSystemMessage(
            UUID tenantId,
            UUID spaceId,
            String body,
            String sourceService,
            String sourceReference,
            String correlationId
    ) {

        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                spaceId,
                "spaceId must not be null"
        );

        RequestContext previous =
                RequestContextHolder.current()
                        .orElse(null);

        requireCompatibleTenant(
                previous,
                tenantId
        );

        RequestContextHolder.set(
                new RequestContext(
                        effectiveCorrelationId(
                                correlationId,
                                previous
                        ),
                        tenantId.toString()
                )
        );

        try {

            ConnectSpace space =
                    spaces.findByIdAndTenantId(
                                    spaceId,
                                    tenantId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "GT Connect space was not found"
                                    )
                            );

            ConnectMessage message =
                    ConnectMessage.automatedSystem(
                            tenantId,
                            space.getId(),
                            body,
                            sourceService,
                            sourceReference
                    );

            /*
             * saveAndFlush is intentional.
             *
             * Tenant enforcement and JPA auditing must execute while the
             * gateway's RequestContext is still active. The previous context
             * is restored immediately afterwards.
             */
            return messages.saveAndFlush(
                    message
            );

        } finally {

            restore(
                    previous
            );
        }
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
                    "Internal GT Connect delivery cannot cross tenant boundaries."
            );
        }
    }

    private static String effectiveCorrelationId(
            String requestedCorrelationId,
            RequestContext previous
    ) {

        if (
                requestedCorrelationId != null
                        && !requestedCorrelationId.isBlank()
        ) {
            return requestedCorrelationId.trim();
        }

        if (
                previous != null
                        && previous.correlationId() != null
                        && !previous.correlationId().isBlank()
        ) {
            return previous.correlationId().trim();
        }

        return "gt-connect-internal-"
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
