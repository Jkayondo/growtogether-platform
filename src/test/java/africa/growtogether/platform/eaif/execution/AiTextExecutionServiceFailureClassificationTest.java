package africa.growtogether.platform.eaif.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import africa.growtogether.platform.eip.AiProviderFailureException;
import org.junit.jupiter.api.Test;

class AiTextExecutionServiceFailureClassificationTest {

    @Test
    void preservesSanitizedProviderHttpCategory() {

        String reason =
                AiTextExecutionService.failureReason(
                        false,
                        new AiProviderFailureException(
                                "AI_PROVIDER_HTTP_401"
                        )
                );

        assertEquals(
                "AI_PROVIDER_HTTP_401",
                reason
        );
    }

    @Test
    void masksUnknownProviderExceptionDetails() {

        String reason =
                AiTextExecutionService.failureReason(
                        false,
                        new IllegalStateException(
                                "private provider details"
                        )
                );

        assertEquals(
                "AI_PROVIDER_EXECUTION_FAILED",
                reason
        );
    }

    @Test
    void distinguishesOutputStorageFailure() {

        String reason =
                AiTextExecutionService.failureReason(
                        true,
                        new IllegalStateException(
                                "private storage details"
                        )
                );

        assertEquals(
                "AI_OUTPUT_STORAGE_FAILED",
                reason
        );
    }
}
