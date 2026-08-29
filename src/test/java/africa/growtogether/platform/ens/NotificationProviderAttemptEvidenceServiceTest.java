package africa.growtogether.platform.ens;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.eip.ExternalProviderDispatchResult;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationProviderAttemptEvidenceServiceTest {

    private NotificationProviderAttemptAllocator allocator;

    private NotificationProviderAttemptRepository attempts;

    private NotificationProviderAttempt attempt;

    private NotificationProviderAttemptEvidenceService service;

    private UUID tenantId;

    private UUID attemptId;

    @BeforeEach
    void setUp() {
        allocator =
                mock(NotificationProviderAttemptAllocator.class);

        attempts =
                mock(NotificationProviderAttemptRepository.class);

        attempt =
                mock(NotificationProviderAttempt.class);

        service =
                new NotificationProviderAttemptEvidenceService(
                        allocator,
                        attempts
                );

        tenantId =
                UUID.randomUUID();

        attemptId =
                UUID.randomUUID();

        when(
                attempts.findByIdAndTenantId(
                        attemptId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(attempt)
        );

        when(
                attempts.saveAndFlush(attempt)
        ).thenReturn(attempt);
    }

    @Test
    void secureAcceptedDoesNotPersistRawProviderMessage() {
        ExternalProviderDispatchResult result =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.ACCEPTED,
                        "request-1",
                        "reference-1",
                        "OK",
                        "secret-token-value echoed by provider"
                );

        service.recordSecureResult(
                tenantId,
                attemptId,
                result
        );

        verify(attempt).submitted(
                "request-1"
        );

        verify(attempt).accepted(
                "reference-1",
                "OK",
                "Provider accepted secure notification"
        );
    }

    @Test
    void secureSubmittedPreservesSubmissionEvidence() {
        ExternalProviderDispatchResult result =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.SUBMITTED,
                        "request-2",
                        null,
                        null,
                        "secret payload echoed here"
                );

        service.recordSecureResult(
                tenantId,
                attemptId,
                result
        );

        verify(attempt).submitted(
                "request-2"
        );
    }

    @Test
    void secureFailedAfterSubmissionUsesSafeMessage() {
        ExternalProviderDispatchResult result =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.FAILED,
                        "request-3",
                        null,
                        "REJECTED",
                        "activation token was rejected: SECRET"
                );

        service.recordSecureResult(
                tenantId,
                attemptId,
                result
        );

        verify(attempt).submitted(
                "request-3"
        );

        verify(attempt).failed(
                "REJECTED",
                "Provider reported secure notification failure"
        );
    }

    @Test
    void secureFailedBeforeSubmissionUsesSafeMessage() {
        ExternalProviderDispatchResult result =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.FAILED,
                        null,
                        null,
                        "INVALID",
                        "raw secret body"
                );

        service.recordSecureResult(
                tenantId,
                attemptId,
                result
        );

        verify(attempt).failedBeforeSubmission(
                "INVALID",
                "Provider reported secure notification failure"
        );
    }

    @Test
    void secureTimeoutAfterSubmissionUsesSafeMessage() {
        ExternalProviderDispatchResult result =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.TIMED_OUT,
                        "request-4",
                        null,
                        null,
                        "timeout while sending SECRET"
                );

        service.recordSecureResult(
                tenantId,
                attemptId,
                result
        );

        verify(attempt).submitted(
                "request-4"
        );

        verify(attempt).timedOut(
                "Secure notification provider response timed out"
        );
    }

    @Test
    void secureTimeoutBeforeSubmissionUsesSafeMessage() {
        ExternalProviderDispatchResult result =
                new ExternalProviderDispatchResult(
                        ExternalProviderDispatchResult.Status.TIMED_OUT,
                        null,
                        null,
                        null,
                        "SECRET appeared in timeout response"
                );

        service.recordSecureResult(
                tenantId,
                attemptId,
                result
        );

        verify(attempt).timedOutBeforeSubmission(
                "Secure notification provider response timed out"
        );
    }
}
