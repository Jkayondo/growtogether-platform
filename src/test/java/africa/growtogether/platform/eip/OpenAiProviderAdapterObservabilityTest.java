package africa.growtogether.platform.eip;

import static org.junit.jupiter.api.Assertions.*;

import africa.growtogether.platform.eaif.execution.AiTextRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OpenAiProviderAdapterObservabilityTest {

    private final ObjectMapper mapper =
            new ObjectMapper();

    private ExternalProviderExecutionContext context() {
        return new ExternalProviderExecutionContext(
                UUID.randomUUID(),
                "OPENAI_PRIMARY",
                "OPENAI_RESPONSES",
                "https://api.openai.com/v1",
                "API_KEY",
                "sk-test-not-a-real-credential",
                null,
                Duration.ofSeconds(30)
        );
    }

    private AiTextRequest request() {
        return new AiTextRequest(
                "configured-model",
                "private controlled test input",
                200
        );
    }

    @Test
    void preservesHttpStatusCategoryWithoutProviderBody() {

        OpenAiProviderAdapter adapter =
                new OpenAiProviderAdapter(
                        mapper,
                        (credential, body, timeout) ->
                                new OpenAiProviderAdapter.Reply(
                                        401,
                                        "{\"error\":\"private provider body\"}"
                                )
                );

        AiProviderFailureException failure =
                assertThrows(
                        AiProviderFailureException.class,
                        () -> adapter.execute(
                                context(),
                                request()
                        )
                );

        assertEquals(
                "AI_PROVIDER_HTTP_401",
                failure.category()
        );

        assertEquals(
                "AI provider execution did not produce a complete text result",
                failure.getMessage()
        );

        assertFalse(
                failure.getMessage()
                        .contains(
                                "private provider body"
                        )
        );
    }

    @Test
    void classifiesTransportTimeoutWithoutNestedDetail() {

        OpenAiProviderAdapter adapter =
                new OpenAiProviderAdapter(
                        mapper,
                        (credential, body, timeout) -> {
                            throw new HttpTimeoutException(
                                    "private endpoint detail"
                            );
                        }
                );

        AiProviderFailureException failure =
                assertThrows(
                        AiProviderFailureException.class,
                        () -> adapter.execute(
                                context(),
                                request()
                        )
                );

        assertEquals(
                "AI_PROVIDER_TIMEOUT",
                failure.category()
        );

        assertFalse(
                failure.getMessage()
                        .contains(
                                "private endpoint detail"
                        )
        );
    }

    @Test
    void classifiesMalformedSuccessfulResponseWithoutBody() {

        OpenAiProviderAdapter adapter =
                new OpenAiProviderAdapter(
                        mapper,
                        (credential, body, timeout) ->
                                new OpenAiProviderAdapter.Reply(
                                        200,
                                        "{not-valid-json"
                                )
                );

        AiProviderFailureException failure =
                assertThrows(
                        AiProviderFailureException.class,
                        () -> adapter.execute(
                                context(),
                                request()
                        )
                );

        assertEquals(
                "AI_PROVIDER_RESPONSE_INVALID",
                failure.category()
        );

        assertFalse(
                failure.getMessage()
                        .contains(
                                "{not-valid-json"
                        )
        );
    }

    @Test
    void rejectsArbitraryFailureCategories() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new AiProviderFailureException(
                        "private provider details"
                )
        );
    }
}
