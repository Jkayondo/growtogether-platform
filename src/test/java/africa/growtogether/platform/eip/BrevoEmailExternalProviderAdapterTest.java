package africa.growtogether.platform.eip;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class BrevoEmailExternalProviderAdapterTest {

    private final ObjectMapper mapper =
            new ObjectMapper();

    @Test
    void acceptedEmailMapsProviderMessageIdAndBuildsExpectedRequest()
            throws Exception {

        AtomicReference<URI> endpoint =
                new AtomicReference<>();

        AtomicReference<String> apiKey =
                new AtomicReference<>();

        AtomicReference<String> body =
                new AtomicReference<>();

        BrevoEmailTransport transport =
                (uri, credential, requestBody, timeout) -> {
                    endpoint.set(uri);
                    apiKey.set(credential);
                    body.set(requestBody);

                    return BrevoEmailTransportResponse.response(
                            201,
                            """
                            {"messageId":"<message-123@relay.test>"}
                            """
                    );
                };

        BrevoEmailExternalProviderAdapter adapter =
                new BrevoEmailExternalProviderAdapter(
                        mapper,
                        transport
                );

        ExternalProviderDispatchResult result =
                adapter.dispatch(
                        context(),
                        request()
                );

        assertThat(result.status())
                .isEqualTo(
                        ExternalProviderDispatchResult.Status.ACCEPTED
                );

        assertThat(result.providerReference())
                .isEqualTo(
                        "<message-123@relay.test>"
                );

        assertThat(endpoint.get())
                .isEqualTo(
                        URI.create(
                                "https://api.brevo.com/v3/smtp/email"
                        )
                );

        assertThat(apiKey.get())
                .isEqualTo(
                        "provider-secret"
                );

        JsonNode sent =
                mapper.readTree(
                        body.get()
                );

        assertThat(
                sent.path("sender")
                        .path("email")
                        .asText()
        ).isEqualTo(
                "no-reply@growtogether.africa"
        );

        assertThat(
                sent.path("sender")
                        .path("name")
                        .asText()
        ).isEqualTo(
                "GrowTogether"
        );

        assertThat(
                sent.path("to")
                        .get(0)
                        .path("email")
                        .asText()
        ).isEqualTo(
                "parent@example.test"
        );

        assertThat(
                sent.path("textContent")
                        .asText()
        ).isEqualTo(
                "secure activation body"
        );

        assertThat(
                sent.path("headers")
                        .path("Idempotency-Key")
                        .asText()
        ).isEqualTo(
                "stable-notification-key"
        );
    }

    @Test
    void providerRejectionIsDefiniteFailureWithoutRawProviderBody() {

        BrevoEmailTransport transport =
                (uri, credential, requestBody, timeout) ->
                        BrevoEmailTransportResponse.response(
                                400,
                                """
                                {"message":"sensitive-provider-error-detail"}
                                """
                        );

        BrevoEmailExternalProviderAdapter adapter =
                new BrevoEmailExternalProviderAdapter(
                        mapper,
                        transport
                );

        ExternalProviderDispatchResult result =
                adapter.dispatch(
                        context(),
                        request()
                );

        assertThat(result.status())
                .isEqualTo(
                        ExternalProviderDispatchResult.Status.FAILED
                );

        assertThat(result.providerCode())
                .isEqualTo(
                        "HTTP_400"
                );

        assertThat(result.providerMessage())
                .isEqualTo(
                        "Brevo rejected transactional email"
                );

        assertThat(result.providerMessage())
                .doesNotContain(
                        "sensitive-provider-error-detail"
                );
    }

    @Test
    void transportTimeoutIsDeliveryAmbiguousTimedOutResult() {

        BrevoEmailTransport transport =
                (uri, credential, requestBody, timeout) ->
                        BrevoEmailTransportResponse
                                .timedOutResponse();

        BrevoEmailExternalProviderAdapter adapter =
                new BrevoEmailExternalProviderAdapter(
                        mapper,
                        transport
                );

        ExternalProviderDispatchResult result =
                adapter.dispatch(
                        context(),
                        request()
                );

        assertThat(result.status())
                .isEqualTo(
                        ExternalProviderDispatchResult.Status.TIMED_OUT
                );
    }

    @Test
    void rejectsPlainHttpBeforeCredentialIsSent() {

        AtomicReference<Boolean> called =
                new AtomicReference<>(
                        false
                );

        BrevoEmailTransport transport =
                (uri, credential, requestBody, timeout) -> {
                    called.set(true);

                    return BrevoEmailTransportResponse.response(
                            201,
                            """
                            {"messageId":"never"}
                            """
                    );
                };

        BrevoEmailExternalProviderAdapter adapter =
                new BrevoEmailExternalProviderAdapter(
                        mapper,
                        transport
                );

        ExternalProviderExecutionContext insecure =
                new ExternalProviderExecutionContext(
                        context().connectorId(),
                        context().connectorCode(),
                        context().connectorType(),
                        "http://api.brevo.test",
                        context().authType(),
                        context().credential(),
                        context().providerConfiguration(),
                        context().requestTimeout()
                );

        assertThatThrownBy(
                () -> adapter.dispatch(
                        insecure,
                        request()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "BREVO_EMAIL connector requires an HTTPS base URL"
                );

        assertThat(called.get())
                .isFalse();
    }

    private ExternalProviderExecutionContext context() {
        return new ExternalProviderExecutionContext(
                java.util.UUID.randomUUID(),
                "BREVO_PRIMARY_EMAIL",
                "BREVO_EMAIL",
                "https://api.brevo.com",
                "API_KEY",
                "provider-secret",
                """
                {
                  "senderEmail":"no-reply@growtogether.africa",
                  "senderName":"GrowTogether"
                }
                """,
                Duration.ofSeconds(30)
        );
    }

    private ExternalProviderDispatchRequest request() {
        return new ExternalProviderDispatchRequest(
                "EMAIL",
                "parent@example.test",
                "Activate your GrowTogether parent account",
                "secure activation body",
                "correlation-1",
                "stable-notification-key",
                Map.of(
                        "notificationId",
                        java.util.UUID.randomUUID()
                                .toString()
                )
        );
    }
}
