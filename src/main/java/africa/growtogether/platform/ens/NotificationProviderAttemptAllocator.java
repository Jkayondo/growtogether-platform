package africa.growtogether.platform.ens;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationProviderAttemptAllocator {

    private final NotificationRequestRepository notifications;
    private final NotificationProviderAttemptRepository attempts;

    public NotificationProviderAttemptAllocator(
            NotificationRequestRepository notifications,
            NotificationProviderAttemptRepository attempts
    ) {
        this.notifications = notifications;
        this.attempts = attempts;
    }

    @Transactional
    public NotificationProviderAttempt allocate(
            UUID tenantId,
            UUID notificationRequestId,
            UUID connectorId,
            UUID routeId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                notificationRequestId,
                "notificationRequestId must not be null"
        );

        Objects.requireNonNull(
                connectorId,
                "connectorId must not be null"
        );

        /*
         * Lock the parent notification row first.
         * Every provider-attempt allocation for the same notification
         * must pass through this lock before reading the latest number.
         */
        notifications
                .findByIdAndTenantIdForUpdate(
                        notificationRequestId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Notification request was not found"
                        )
                );

        int nextAttemptNumber =
                attempts
                        .findTopByTenantIdAndNotificationRequestIdOrderByAttemptNumberDesc(
                                tenantId,
                                notificationRequestId
                        )
                        .map(
                                latest -> nextNumber(
                                        latest.attemptNumber()
                                )
                        )
                        .orElse(1);

        NotificationProviderAttempt attempt =
                new NotificationProviderAttempt(
                        tenantId,
                        notificationRequestId,
                        connectorId,
                        routeId,
                        nextAttemptNumber
                );

        return attempts.saveAndFlush(attempt);
    }

    private static int nextNumber(
            int latestAttemptNumber
    ) {
        if (latestAttemptNumber == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "Provider attempt number limit reached"
            );
        }

        return latestAttemptNumber + 1;
    }
}
