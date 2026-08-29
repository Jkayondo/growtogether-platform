package africa.growtogether.platform.ens;

import africa.growtogether.platform.eip.ExternalProviderDispatchResult;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationProviderAttemptEvidenceService {

    private final NotificationProviderAttemptAllocator allocator;
    private final NotificationProviderAttemptRepository attempts;

    public NotificationProviderAttemptEvidenceService(
            NotificationProviderAttemptAllocator allocator,
            NotificationProviderAttemptRepository attempts
    ) {
        this.allocator = allocator;
        this.attempts = attempts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationProviderAttempt allocate(
            UUID tenantId,
            UUID notificationRequestId,
            UUID connectorId,
            UUID routeId
    ) {
        return allocator.allocate(
                tenantId,
                notificationRequestId,
                connectorId,
                routeId
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationProviderAttempt recordResult(
            UUID tenantId,
            UUID attemptId,
            ExternalProviderDispatchResult result
    ) {
        Objects.requireNonNull(
                result,
                "result must not be null"
        );

        NotificationProviderAttempt attempt =
                requireAttempt(
                        tenantId,
                        attemptId
                );

        switch (result.status()) {
            case SUBMITTED ->
                    attempt.submitted(
                            result.providerRequestId()
                    );

            case ACCEPTED -> {
                attempt.submitted(
                        result.providerRequestId()
                );

                attempt.accepted(
                        result.providerReference(),
                        result.providerCode(),
                        result.providerMessage()
                );
            }

            case FAILED -> {
                if (hasSubmissionEvidence(
                        result.providerRequestId()
                )) {
                    attempt.submitted(
                            result.providerRequestId()
                    );

                    attempt.failed(
                            result.providerCode(),
                            result.providerMessage()
                    );

                } else {
                    attempt.failedBeforeSubmission(
                            result.providerCode(),
                            result.providerMessage()
                    );
                }
            }

            case TIMED_OUT -> {
                if (hasSubmissionEvidence(
                        result.providerRequestId()
                )) {
                    attempt.submitted(
                            result.providerRequestId()
                    );

                    attempt.timedOut(
                            result.providerMessage()
                    );

                } else {
                    attempt.timedOutBeforeSubmission(
                            result.providerMessage()
                    );
                }
            }
        }

        return attempts.saveAndFlush(
                attempt
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationProviderAttempt recordSecureResult(
            UUID tenantId,
            UUID attemptId,
            ExternalProviderDispatchResult result
    ) {
        Objects.requireNonNull(
                result,
                "result must not be null"
        );

        String safeProviderMessage =
                switch (result.status()) {
                    case SUBMITTED -> null;

                    case ACCEPTED ->
                            "Provider accepted secure notification";

                    case FAILED ->
                            "Provider reported secure notification failure";

                    case TIMED_OUT ->
                            "Secure notification provider response timed out";
                };

        ExternalProviderDispatchResult sanitized =
                new ExternalProviderDispatchResult(
                        result.status(),
                        result.providerRequestId(),
                        result.providerReference(),
                        result.providerCode(),
                        safeProviderMessage
                );

        return recordResult(
                tenantId,
                attemptId,
                sanitized
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationProviderAttempt recordExecutionFailure(
            UUID tenantId,
            UUID attemptId,
            String errorCode,
            String safeErrorMessage
    ) {
        NotificationProviderAttempt attempt =
                requireAttempt(
                        tenantId,
                        attemptId
                );

        attempt.failedBeforeSubmission(
                errorCode,
                safeErrorMessage
        );

        return attempts.saveAndFlush(
                attempt
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationProviderAttempt recordExecutionTimeout(
            UUID tenantId,
            UUID attemptId,
            String safeErrorMessage
    ) {
        NotificationProviderAttempt attempt =
                requireAttempt(
                        tenantId,
                        attemptId
                );

        attempt.timedOutBeforeSubmission(
                safeErrorMessage
        );

        return attempts.saveAndFlush(
                attempt
        );
    }

    private NotificationProviderAttempt requireAttempt(
            UUID tenantId,
            UUID attemptId
    ) {
        Objects.requireNonNull(
                tenantId,
                "tenantId must not be null"
        );

        Objects.requireNonNull(
                attemptId,
                "attemptId must not be null"
        );

        return attempts
                .findByIdAndTenantId(
                        attemptId,
                        tenantId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Notification provider attempt was not found"
                        )
                );
    }

    private static boolean hasSubmissionEvidence(
            String providerRequestId
    ) {
        return providerRequestId != null
                && !providerRequestId.isBlank();
    }
}
