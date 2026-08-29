package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.ens.integration.EnsAuditRecorder;
import africa.growtogether.platform.ens.integration.EnsConfigurationGateway;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDispatchLifecycleService {

    private final NotificationRequestRepository notifications;
    private final EnsConfigurationGateway configuration;
    private final EnsAuditRecorder audit;

    public NotificationDispatchLifecycleService(
            NotificationRequestRepository notifications,
            EnsConfigurationGateway configuration,
            EnsAuditRecorder audit
    ) {
        this.notifications = notifications;
        this.configuration = configuration;
        this.audit = audit;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationDispatchWorkItem beginProcessing(
            UUID tenantId,
            UUID notificationRequestId
    ) {
        NotificationRequest notification =
                lock(
                        tenantId,
                        notificationRequestId
                );

        String correlationId =
                effectiveCorrelationId(
                        notification
                );

        return withRequestContext(
                tenantId,
                correlationId,
                () -> {
                    /*
                     * processing() accepts only QUEUED or RETRYING.
                     * Because the notification row is locked here,
                     * a second dispatcher cannot begin the same
                     * processing cycle concurrently.
                     */
                    notification.processing();

                    notifications.saveAndFlush(
                            notification
                    );

                    audit.processing(
                            notification
                    );

                    return snapshot(
                            notification,
                            correlationId
                    );
                }
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationStatus markSent(
            UUID tenantId,
            UUID notificationRequestId,
            String providerReference
    ) {
        NotificationRequest notification =
                lock(
                        tenantId,
                        notificationRequestId
                );

        return withRequestContext(
                tenantId,
                effectiveCorrelationId(notification),
                () -> {
                    notification.sent(
                            providerReference
                    );

                    notifications.saveAndFlush(
                            notification
                    );

                    audit.sent(
                            notification
                    );

                    return notification.notificationStatus();
                }
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationStatus markFailed(
            UUID tenantId,
            UUID notificationRequestId,
            String safeError
    ) {
        NotificationRequest notification =
                lock(
                        tenantId,
                        notificationRequestId
                );

        return withRequestContext(
                tenantId,
                effectiveCorrelationId(notification),
                () -> {
                    int maxAttempts =
                            configuration.maxAttempts(
                                    tenantId
                            );

                    long backoffSeconds =
                            configuration.retryBackoffSeconds(
                                    tenantId,
                                    notification.attemptCount() + 1
                            );

                    notification.fail(
                            safeError,
                            Instant.now().plusSeconds(
                                    backoffSeconds
                            ),
                            maxAttempts
                    );

                    notifications.saveAndFlush(
                            notification
                    );

                    audit.failed(
                            notification
                    );

                    return notification.notificationStatus();
                }
        );
    }

    private NotificationRequest lock(
            UUID tenantId,
            UUID notificationRequestId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                notificationRequestId,
                "notificationRequestId must not be null"
        );

        return notifications
                .findByIdAndTenantIdForUpdate(
                        notificationRequestId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Notification request was not found"
                        )
                );
    }

    private static NotificationDispatchWorkItem snapshot(
            NotificationRequest notification,
            String correlationId
    ) {
        return new NotificationDispatchWorkItem(
                notification.id(),
                notification.getTenantId(),
                notification.definitionCode(),
                notification.recipient(),
                notification.channel(),
                notification.priority(),
                notification.subject(),
                notification.body(),
                correlationId,
                notification.sourceService(),
                notification.sourceReference(),
                notification.attemptCount()
        );
    }

    private static String effectiveCorrelationId(
            NotificationRequest notification
    ) {
        String existing =
                notification.correlationId();

        if (
                existing != null
                && !existing.isBlank()
        ) {
            return existing.trim();
        }

        return "ens-dispatch-"
                + notification.id();
    }

    private static <T> T withRequestContext(
            UUID tenantId,
            String correlationId,
            Supplier<T> work
    ) {
        RequestContext previous =
                RequestContextHolder.current()
                        .orElse(null);

        RequestContextHolder.set(
                new RequestContext(
                        correlationId,
                        tenantId.toString()
                )
        );

        try {
            return work.get();

        } finally {
            if (previous == null) {
                RequestContextHolder.clear();

            } else {
                RequestContextHolder.set(
                        previous
                );
            }
        }
    }
}
