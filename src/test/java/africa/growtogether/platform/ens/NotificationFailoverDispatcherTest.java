package africa.growtogether.platform.ens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.eip.ExternalProviderDispatchRequest;
import africa.growtogether.platform.eip.ExternalProviderDispatchResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationFailoverDispatcherTest {

    private NotificationDispatchLifecycleService lifecycle;
    private NotificationRouteResolver routes;
    private NotificationProviderAttemptEvidenceService evidence;
    private NotificationExternalProviderExecutionService execution;
    private NotificationSecurePayloadService securePayloads;
    private NotificationFailoverDispatcher dispatcher;

    private UUID tenantId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        lifecycle =
                mock(NotificationDispatchLifecycleService.class);

        routes =
                mock(NotificationRouteResolver.class);

        evidence =
                mock(NotificationProviderAttemptEvidenceService.class);

        execution =
                mock(NotificationExternalProviderExecutionService.class);

        securePayloads =
                mock(NotificationSecurePayloadService.class);

        dispatcher =
                new NotificationFailoverDispatcher(
                        lifecycle,
                        routes,
                        evidence,
                        execution,
                        securePayloads
                );

        tenantId =
                UUID.randomUUID();

        notificationId =
                UUID.randomUUID();
    }

    @Test
    void firstProviderSuccessStopsWithoutFailover() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationRouteCandidate backup =
                route(
                        2,
                        true
                );

        NotificationProviderAttempt attempt =
                attempt(
                        1
                );

        when(
                lifecycle.beginProcessing(
                        tenantId,
                        notificationId
                )
        ).thenReturn(workItem);

        when(
                routes.resolve(
                        tenantId,
                        NotificationChannel.WHATSAPP
                )
        ).thenReturn(
                List.of(
                        primary,
                        backup
                )
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(attempt);

        ExternalProviderDispatchResult accepted =
                accepted(
                        "request-primary",
                        "reference-primary"
                );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(accepted);

        when(
                lifecycle.markSent(
                        tenantId,
                        notificationId,
                        "reference-primary"
                )
        ).thenReturn(NotificationStatus.SENT);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.SENT);

        verify(execution, never())
                .dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );

        verify(lifecycle, never())
                .markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                );
    }

    @Test
    void explicitProviderFailureFailsOverToSecondProvider() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationRouteCandidate backup =
                route(
                        2,
                        true
                );

        NotificationProviderAttempt firstAttempt =
                attempt(
                        1
                );

        NotificationProviderAttempt secondAttempt =
                attempt(
                        2
                );

        prepare(
                workItem,
                List.of(
                        primary,
                        backup
                )
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(firstAttempt);

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        backup.connectorId(),
                        backup.routeId()
                )
        ).thenReturn(secondAttempt);

        ExternalProviderDispatchResult failed =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.FAILED,
                        "request-primary",
                        null,
                        "PROVIDER_FAILURE",
                        "Rejected"
                );

        ExternalProviderDispatchResult accepted =
                accepted(
                        "request-backup",
                        "reference-backup"
                );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(failed);

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(accepted);

        when(
                lifecycle.markSent(
                        tenantId,
                        notificationId,
                        "reference-backup"
                )
        ).thenReturn(NotificationStatus.SENT);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.SENT);

        verify(execution)
                .dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );

        verify(execution)
                .dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );

        verify(lifecycle, never())
                .markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                );
    }

    @Test
    void failoverDisabledStopsAfterExplicitFailure() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        false
                );

        NotificationRouteCandidate backup =
                route(
                        2,
                        true
                );

        prepare(
                workItem,
                List.of(
                        primary,
                        backup
                )
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenAnswer(
                invocation -> attempt(1)
        );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.FAILED,
                        "request-primary",
                        null,
                        "FAILED",
                        "Rejected"
                )
        );

        when(
                lifecycle.markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                )
        ).thenReturn(NotificationStatus.RETRYING);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.RETRYING);

        verify(execution, never())
                .dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );
    }

    @Test
    void timeoutDoesNotCrossProviderFailover() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationRouteCandidate backup =
                route(
                        2,
                        true
                );

        prepare(
                workItem,
                List.of(
                        primary,
                        backup
                )
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenAnswer(
                invocation -> attempt(1)
        );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.TIMED_OUT,
                        "request-primary",
                        null,
                        null,
                        "Timeout"
                )
        );

        when(
                lifecycle.markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                )
        ).thenReturn(NotificationStatus.RETRYING);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.RETRYING);

        verify(execution, never())
                .dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );
    }

    @Test
    void runtimeExecutionExceptionDoesNotCrossProviderFailover() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationRouteCandidate backup =
                route(
                        2,
                        true
                );

        NotificationProviderAttempt attempt =
                attempt(
                        1
                );

        prepare(
                workItem,
                List.of(
                        primary,
                        backup
                )
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(attempt);

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenThrow(
                new IllegalStateException(
                        "simulated provider execution failure"
                )
        );

        when(
                lifecycle.markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                )
        ).thenReturn(NotificationStatus.RETRYING);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.RETRYING);

        verify(evidence)
                .recordExecutionFailure(
                        tenantId,
                        attempt.id(),
                        "EXECUTION_ERROR",
                        "External notification provider execution failed"
                );

        verify(execution, never())
                .dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );
    }

    @Test
    void idempotencyKeyIsStableAcrossRetriesForSameConnector() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationProviderAttempt firstAttempt =
                attempt(
                        1
                );

        NotificationProviderAttempt retryAttempt =
                attempt(
                        2
                );

        prepare(
                workItem,
                List.of(primary)
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(
                firstAttempt,
                retryAttempt
        );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.TIMED_OUT,
                        null,
                        null,
                        null,
                        "Timeout"
                )
        );

        when(
                lifecycle.markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                )
        ).thenReturn(NotificationStatus.RETRYING);

        dispatcher.dispatch(
                tenantId,
                notificationId
        );

        dispatcher.dispatch(
                tenantId,
                notificationId
        );

        ArgumentCaptor<ExternalProviderDispatchRequest> captor =
                ArgumentCaptor.forClass(
                        ExternalProviderDispatchRequest.class
                );

        verify(execution, times(2))
                .dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        captor.capture()
                );

        List<ExternalProviderDispatchRequest> captured =
                captor.getAllValues();

        assertThat(captured)
                .hasSize(2);

        assertThat(
                captured.get(0).idempotencyKey()
        ).isEqualTo(
                captured.get(1).idempotencyKey()
        );

        assertThat(
                captured.get(0).idempotencyKey()
        ).isEqualTo(
                "ens-notification:"
                        + notificationId
                        + ":connector:"
                        + primary.connectorId()
        );
    }

    @Test
    void noUsableRoutesMarksNotificationFailedWithoutProviderAttempt() {
        NotificationDispatchWorkItem workItem =
                workItem();

        prepare(
                workItem,
                List.of()
        );

        when(
                lifecycle.markFailed(
                        tenantId,
                        notificationId,
                        "No usable notification provider route configured"
                )
        ).thenReturn(NotificationStatus.RETRYING);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.RETRYING);

        verify(evidence, never())
                .allocate(
                        any(),
                        any(),
                        any(),
                        any()
                );

        verify(execution, never())
                .dispatch(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void allEligibleProvidersFailMarksNotificationFailed() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationRouteCandidate backup =
                route(
                        2,
                        true
                );

        prepare(
                workItem,
                List.of(
                        primary,
                        backup
                )
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenAnswer(
                invocation -> attempt(1)
        );

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        backup.connectorId(),
                        backup.routeId()
                )
        ).thenAnswer(
                invocation -> attempt(2)
        );

        ExternalProviderDispatchResult failed =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.FAILED,
                        "provider-request",
                        null,
                        "FAILED",
                        "Rejected"
                );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(failed);

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(failed);

        when(
                lifecycle.markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                )
        ).thenReturn(NotificationStatus.DEAD_LETTER);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(
                        NotificationStatus.DEAD_LETTER
                );

        verify(execution)
                .dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );

        verify(execution)
                .dispatch(
                        eq(tenantId),
                        eq(backup.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                );
    }

    @Test
    void secureNotificationUsesDecryptedBodyAndRetiresAfterSent() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationProviderAttempt attempt =
                attempt(
                        1
                );

        prepare(
                workItem,
                List.of(primary)
        );

        when(
                securePayloads.hasSecurePayload(
                        tenantId,
                        notificationId
                )
        ).thenReturn(true);

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(attempt);

        String secureBody =
                "Activate with one-time-secret-value";

        when(
                securePayloads.resolveIfPresent(
                        eq(tenantId),
                        eq(notificationId),
                        any(Instant.class)
                )
        ).thenReturn(
                Optional.of(secureBody)
        );

        ExternalProviderDispatchResult accepted =
                accepted(
                        "secure-request",
                        "secure-reference"
                );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(accepted);

        when(
                lifecycle.markSent(
                        tenantId,
                        notificationId,
                        "secure-reference"
                )
        ).thenReturn(NotificationStatus.SENT);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.SENT);

        ArgumentCaptor<ExternalProviderDispatchRequest> captor =
                ArgumentCaptor.forClass(
                        ExternalProviderDispatchRequest.class
                );

        verify(execution)
                .dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        captor.capture()
                );

        ExternalProviderDispatchRequest providerRequest =
                captor.getValue();

        assertThat(providerRequest.body())
                .isEqualTo(secureBody);

        assertThat(providerRequest.body())
                .isNotEqualTo(workItem.body());

        verify(evidence)
                .recordSecureResult(
                        tenantId,
                        attempt.id(),
                        accepted
                );

        verify(evidence, never())
                .recordResult(
                        eq(tenantId),
                        any(UUID.class),
                        any(ExternalProviderDispatchResult.class)
                );

        verify(securePayloads)
                .retireIfPresent(
                        eq(tenantId),
                        eq(notificationId),
                        any(Instant.class)
                );
    }

    @Test
    void unavailableSecurePayloadBlocksProviderTraffic() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationProviderAttempt attempt =
                attempt(
                        1
                );

        prepare(
                workItem,
                List.of(primary)
        );

        when(
                securePayloads.hasSecurePayload(
                        tenantId,
                        notificationId
                )
        ).thenReturn(true);

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(attempt);

        when(
                securePayloads.resolveIfPresent(
                        eq(tenantId),
                        eq(notificationId),
                        any(Instant.class)
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                lifecycle.markFailed(
                        tenantId,
                        notificationId,
                        "Secure notification payload is unavailable"
                )
        ).thenReturn(NotificationStatus.RETRYING);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.RETRYING);

        verify(evidence)
                .recordExecutionFailure(
                        tenantId,
                        attempt.id(),
                        "SECURE_PAYLOAD_UNAVAILABLE",
                        "Secure notification payload is unavailable"
                );

        verify(execution, never())
                .dispatch(
                        any(),
                        any(),
                        any()
                );

        verify(evidence, never())
                .recordSecureResult(
                        any(),
                        any(),
                        any()
                );

        verify(securePayloads, never())
                .retireIfPresent(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void failedSecureDispatchUsesSecureEvidenceAndDoesNotRetire() {
        NotificationDispatchWorkItem workItem =
                workItem();

        NotificationRouteCandidate primary =
                route(
                        1,
                        true
                );

        NotificationProviderAttempt attempt =
                attempt(
                        1
                );

        prepare(
                workItem,
                List.of(primary)
        );

        when(
                securePayloads.hasSecurePayload(
                        tenantId,
                        notificationId
                )
        ).thenReturn(true);

        when(
                evidence.allocate(
                        tenantId,
                        notificationId,
                        primary.connectorId(),
                        primary.routeId()
                )
        ).thenReturn(attempt);

        when(
                securePayloads.resolveIfPresent(
                        eq(tenantId),
                        eq(notificationId),
                        any(Instant.class)
                )
        ).thenReturn(
                Optional.of(
                        "secure activation body"
                )
        );

        ExternalProviderDispatchResult failed =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.FAILED,
                        "secure-request-failed",
                        null,
                        "REJECTED",
                        "provider may echo SECRET here"
                );

        when(
                execution.dispatch(
                        eq(tenantId),
                        eq(primary.connectorId()),
                        any(ExternalProviderDispatchRequest.class)
                )
        ).thenReturn(failed);

        when(
                lifecycle.markFailed(
                        eq(tenantId),
                        eq(notificationId),
                        anyString()
                )
        ).thenReturn(NotificationStatus.RETRYING);

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        assertThat(status)
                .isEqualTo(NotificationStatus.RETRYING);

        verify(evidence)
                .recordSecureResult(
                        tenantId,
                        attempt.id(),
                        failed
                );

        verify(evidence, never())
                .recordResult(
                        eq(tenantId),
                        any(UUID.class),
                        any(ExternalProviderDispatchResult.class)
                );

        verify(securePayloads, never())
                .retireIfPresent(
                        any(),
                        any(),
                        any()
                );
    }

    private void prepare(
            NotificationDispatchWorkItem workItem,
            List<NotificationRouteCandidate> candidates
    ) {
        when(
                lifecycle.beginProcessing(
                        tenantId,
                        notificationId
                )
        ).thenReturn(workItem);

        when(
                routes.resolve(
                        tenantId,
                        workItem.channel()
                )
        ).thenReturn(candidates);
    }

    private NotificationDispatchWorkItem workItem() {
        return new NotificationDispatchWorkItem(
                notificationId,
                tenantId,
                "PARENT_ACTIVATION",
                "+256700000000",
                NotificationChannel.WHATSAPP,
                NotificationPriority.NORMAL,
                "Welcome",
                "Activate your GrowTogether account",
                "ens-test-correlation",
                "GT_SCHOOL",
                "ADMISSION-001",
                1
        );
    }

    private NotificationRouteCandidate route(
            int priority,
            boolean failoverEnabled
    ) {
        return new NotificationRouteCandidate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CONNECTOR-" + priority,
                "WHATSAPP",
                priority,
                failoverEnabled
        );
    }

    private NotificationProviderAttempt attempt(
            int attemptNumber
    ) {
        NotificationProviderAttempt attempt =
                mock(NotificationProviderAttempt.class);

        when(attempt.id())
                .thenReturn(
                        UUID.randomUUID()
                );

        when(attempt.attemptNumber())
                .thenReturn(
                        attemptNumber
                );

        return attempt;
    }

    private ExternalProviderDispatchResult accepted(
            String requestId,
            String reference
    ) {
        return new ExternalProviderDispatchResult(
                ExternalProviderDispatchResult.Status.ACCEPTED,
                requestId,
                reference,
                "200",
                "Accepted"
        );
    }
}
