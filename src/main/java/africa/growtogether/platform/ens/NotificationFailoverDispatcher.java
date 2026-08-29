package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eip.ExternalProviderDispatchRequest;
import africa.growtogether.platform.eip.ExternalProviderDispatchResult;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationFailoverDispatcher {

    private final NotificationDispatchLifecycleService lifecycle;
    private final NotificationRouteResolver routeResolver;
    private final NotificationProviderAttemptEvidenceService evidence;
    private final NotificationExternalProviderExecutionService execution;
    private final NotificationSecurePayloadService securePayloads;

    public NotificationFailoverDispatcher(
            NotificationDispatchLifecycleService lifecycle,
            NotificationRouteResolver routeResolver,
            NotificationProviderAttemptEvidenceService evidence,
            NotificationExternalProviderExecutionService execution,
            NotificationSecurePayloadService securePayloads
    ) {
        this.lifecycle = lifecycle;
        this.routeResolver = routeResolver;
        this.evidence = evidence;
        this.execution = execution;
        this.securePayloads = securePayloads;
    }

    /*
     * A complete provider dispatch cycle must never inherit a surrounding
     * database transaction. All database mutations are delegated to short
     * REQUIRES_NEW services, while provider traffic occurs outside them.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public NotificationStatus dispatch(
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

        /*
         * This short transaction moves QUEUED/RETRYING -> PROCESSING
         * exactly once for the notification processing cycle.
         */
        NotificationDispatchWorkItem workItem =
                lifecycle.beginProcessing(
                        tenantId,
                        notificationRequestId
                );

        RequestContext previous =
                RequestContextHolder.current()
                        .orElse(null);

        /*
         * Provider-attempt persistence and EIP execution may themselves
         * require tenant/correlation evidence even though this is not an
         * interactive HTTP request.
         */
        RequestContextHolder.set(
                new RequestContext(
                        workItem.correlationId(),
                        tenantId.toString()
                )
        );

        try {
            /*
             * The notification is already PROCESSING here. A secure payload
             * may only be attached while the notification is QUEUED, so this
             * classification remains stable for the complete dispatch cycle.
             */
            boolean secureNotification =
                    securePayloads.hasSecurePayload(
                            tenantId,
                            notificationRequestId
                    );

            List<NotificationRouteCandidate> candidates =
                    routeResolver.resolve(
                            tenantId,
                            workItem.channel()
                    );

            if (candidates.isEmpty()) {
                return lifecycle.markFailed(
                        tenantId,
                        notificationRequestId,
                        "No usable notification provider route configured"
                );
            }

            String lastSafeError =
                    "All eligible notification provider routes failed";

            for (
                    int index = 0;
                    index < candidates.size();
                    index++
            ) {
                NotificationRouteCandidate candidate =
                        candidates.get(index);

                NotificationProviderAttempt attempt =
                        evidence.allocate(
                                tenantId,
                                notificationRequestId,
                                candidate.connectorId(),
                                candidate.routeId()
                        );

                String providerBody =
                        workItem.body();

                if (secureNotification) {
                    try {
                        /*
                         * Dispatcher execution is NOT_SUPPORTED. Decryption
                         * therefore happens outside any database transaction
                         * and only immediately before the provider request is
                         * constructed.
                         */
                        providerBody =
                                securePayloads
                                        .resolveIfPresent(
                                                tenantId,
                                                notificationRequestId,
                                                Instant.now()
                                        )
                                        .orElseThrow(
                                                () ->
                                                        new NotificationSecurePayloadException(
                                                                "Secure notification payload "
                                                                        + "was not found"
                                                        )
                                        );

                    } catch (RuntimeException exception) {
                        /*
                         * This failure occurred before provider traffic.
                         * Never persist exception text because crypto/database
                         * failures may contain sensitive implementation detail.
                         */
                        evidence.recordExecutionFailure(
                                tenantId,
                                attempt.id(),
                                "SECURE_PAYLOAD_UNAVAILABLE",
                                "Secure notification payload is unavailable"
                        );

                        lastSafeError =
                                "Secure notification payload is unavailable";

                        break;
                    }
                }

                ExternalProviderDispatchRequest request =
                        request(
                                workItem,
                                candidate,
                                attempt,
                                providerBody
                        );

                ExternalProviderDispatchResult result;

                try {
                    /*
                     * This service is NOT_SUPPORTED and therefore performs
                     * provider traffic without a database transaction.
                     */
                    result =
                            execution.dispatch(
                                    tenantId,
                                    candidate.connectorId(),
                                    request
                            );

                } catch (RuntimeException exception) {
                    /*
                     * Never persist raw exception text here because provider
                     * SDK/client exceptions may contain endpoint, credential,
                     * token or request details.
                     */
                    evidence.recordExecutionFailure(
                            tenantId,
                            attempt.id(),
                            "EXECUTION_ERROR",
                            "External notification provider execution failed"
                    );

                    lastSafeError =
                            "Notification provider execution failed";

                    /*
                     * An unclassified execution exception is delivery-
                     * ambiguous. GT cannot prove that the provider did not
                     * receive the request, so immediate cross-provider
                     * failover could create duplicate delivery.
                     *
                     * Typed pre-submission failures may be introduced later
                     * by the EIP adapter contract when real provider adapters
                     * are implemented.
                     */
                    break;
                }

                if (secureNotification) {
                    evidence.recordSecureResult(
                            tenantId,
                            attempt.id(),
                            result
                    );

                } else {
                    evidence.recordResult(
                            tenantId,
                            attempt.id(),
                            result
                    );
                }

                if (result.successful()) {
                    NotificationStatus sentStatus =
                            lifecycle.markSent(
                                    tenantId,
                                    notificationRequestId,
                                    providerReference(
                                            result
                                    )
                            );

                    /*
                     * Keep the encrypted payload available throughout retries
                     * and failover. Retire it only after ENS has successfully
                     * completed the notification-level SENT transition.
                     */
                    if (
                            secureNotification
                                    && sentStatus == NotificationStatus.SENT
                    ) {
                        securePayloads.retireIfPresent(
                                tenantId,
                                notificationRequestId,
                                Instant.now()
                        );
                    }

                    return sentStatus;
                }

                lastSafeError =
                        safeFailureDescription(
                                result
                        );

                /*
                 * TIMED_OUT is delivery-ambiguous. The provider may have
                 * accepted the request while GT lost the response.
                 *
                 * Do not send the same logical notification through another
                 * provider immediately. Notification-level retry may later
                 * use the same connector with the same stable idempotency key.
                 */
                if (
                        result.status()
                                == ExternalProviderDispatchResult.Status.TIMED_OUT
                ) {
                    break;
                }

                /*
                 * FAILED is an explicit provider failure and is therefore
                 * eligible for configured failover.
                 */
                if (
                        !shouldFailover(
                                candidate,
                                index,
                                candidates.size()
                        )
                ) {
                    break;
                }
            }

            /*
             * All eligible routes have failed, or a route explicitly
             * prohibited further failover. Notification-level retry/backoff
             * remains the existing ENS responsibility.
             */
            return lifecycle.markFailed(
                    tenantId,
                    notificationRequestId,
                    lastSafeError
            );

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

    private static ExternalProviderDispatchRequest request(
            NotificationDispatchWorkItem workItem,
            NotificationRouteCandidate candidate,
            NotificationProviderAttempt attempt,
            String providerBody
    ) {
        Map<String, String> attributes =
                new HashMap<>();

        attributes.put(
                "notificationId",
                workItem.notificationId()
                        .toString()
        );

        attributes.put(
                "definitionCode",
                workItem.definitionCode()
        );

        attributes.put(
                "sourceService",
                workItem.sourceService()
        );

        attributes.put(
                "processingAttempt",
                Integer.toString(
                        workItem.processingAttempt()
                )
        );

        attributes.put(
                "providerAttempt",
                Integer.toString(
                        attempt.attemptNumber()
                )
        );

        attributes.put(
                "connectorCode",
                candidate.connectorCode()
        );

        attributes.put(
                "connectorType",
                candidate.connectorType()
        );

        attributes.put(
                "routeId",
                candidate.routeId()
                        .toString()
        );

        if (
                workItem.sourceReference() != null
                && !workItem.sourceReference().isBlank()
        ) {
            attributes.put(
                    "sourceReference",
                    workItem.sourceReference()
            );
        }

        return new ExternalProviderDispatchRequest(
                workItem.channel().name(),
                workItem.recipient(),
                workItem.subject(),
                providerBody,
                workItem.correlationId(),
                idempotencyKey(
                        workItem,
                        candidate
                ),
                Map.copyOf(attributes)
        );
    }

    private static String idempotencyKey(
            NotificationDispatchWorkItem workItem,
            NotificationRouteCandidate candidate
    ) {
        /*
         * Stable for the same logical notification/provider connector,
         * including notification-level retries.
         *
         * A retry must not receive a brand-new provider idempotency key,
         * otherwise a response-loss timeout could cause duplicate delivery.
         */
        return "ens-notification:"
                + workItem.notificationId()
                + ":connector:"
                + candidate.connectorId();
    }

    private static boolean shouldFailover(
            NotificationRouteCandidate candidate,
            int currentIndex,
            int candidateCount
    ) {
        return candidate.failoverEnabled()
                && currentIndex + 1 < candidateCount;
    }

    private static String providerReference(
            ExternalProviderDispatchResult result
    ) {
        String reference =
                normalize(
                        result.providerReference()
                );

        if (reference != null) {
            return reference;
        }

        return normalize(
                result.providerRequestId()
        );
    }

    private static String safeFailureDescription(
            ExternalProviderDispatchResult result
    ) {
        return switch (result.status()) {
            case FAILED ->
                    "Notification provider reported failure";

            case TIMED_OUT ->
                    "Notification provider timed out";

            case SUBMITTED, ACCEPTED ->
                    "Notification provider dispatch succeeded";
        };
    }

    private static String normalize(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
