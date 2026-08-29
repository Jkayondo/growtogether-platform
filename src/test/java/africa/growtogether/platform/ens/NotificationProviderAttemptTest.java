package africa.growtogether.platform.ens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationProviderAttemptTest {

    @Test
    void newAttemptStartsCreated() {
        NotificationProviderAttempt attempt =
                attempt();

        assertThat(attempt.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.CREATED
                );

        assertThat(attempt.submittedAt())
                .isNull();

        assertThat(attempt.completedAt())
                .isNull();
    }

    @Test
    void submittedAttemptCanBeAccepted() {
        NotificationProviderAttempt attempt =
                attempt();

        attempt.submitted("provider-request-1");

        attempt.accepted(
                "provider-reference-1",
                "200",
                "Accepted"
        );

        assertThat(attempt.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.ACCEPTED
                );

        assertThat(attempt.providerRequestId())
                .isEqualTo("provider-request-1");

        assertThat(attempt.providerReference())
                .isEqualTo("provider-reference-1");

        assertThat(attempt.submittedAt())
                .isNotNull();

        assertThat(attempt.completedAt())
                .isNotNull();
    }

    @Test
    void submittedAttemptCanFail() {
        NotificationProviderAttempt attempt =
                attempt();

        attempt.submitted("provider-request-2");

        attempt.failed(
                "500",
                "Provider rejected request"
        );

        assertThat(attempt.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.FAILED
                );

        assertThat(attempt.providerRequestId())
                .isEqualTo("provider-request-2");

        assertThat(attempt.completedAt())
                .isNotNull();
    }

    @Test
    void submittedAttemptCanTimeOut() {
        NotificationProviderAttempt attempt =
                attempt();

        attempt.submitted("provider-request-3");

        attempt.timedOut(
                "Provider response timed out"
        );

        assertThat(attempt.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.TIMED_OUT
                );

        assertThat(attempt.completedAt())
                .isNotNull();
    }

    @Test
    void failureBeforeSubmissionIsRecordedHonestly() {
        NotificationProviderAttempt attempt =
                attempt();

        attempt.failedBeforeSubmission(
                "EXECUTION_ERROR",
                "Provider execution failed"
        );

        assertThat(attempt.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.FAILED
                );

        assertThat(attempt.providerRequestId())
                .isNull();

        assertThat(attempt.submittedAt())
                .isNull();

        assertThat(attempt.completedAt())
                .isNotNull();
    }

    @Test
    void timeoutBeforeSubmissionIsRecordedHonestly() {
        NotificationProviderAttempt attempt =
                attempt();

        attempt.timedOutBeforeSubmission(
                "No submission confirmation received"
        );

        assertThat(attempt.attemptStatus())
                .isEqualTo(
                        NotificationProviderAttemptStatus.TIMED_OUT
                );

        assertThat(attempt.providerRequestId())
                .isNull();

        assertThat(attempt.submittedAt())
                .isNull();

        assertThat(attempt.completedAt())
                .isNotNull();
    }

    @Test
    void preSubmissionFailureCannotOverwriteSubmittedAttempt() {
        NotificationProviderAttempt attempt =
                attempt();

        attempt.submitted("provider-request-4");

        assertThatThrownBy(
                () -> attempt.failedBeforeSubmission(
                        "EXECUTION_ERROR",
                        "Should not overwrite submission evidence"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "Expected CREATED"
                );
    }

    private NotificationProviderAttempt attempt() {
        return new NotificationProviderAttempt(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                1
        );
    }
}
