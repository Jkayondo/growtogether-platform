package africa.growtogether.platform.eip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Real Brevo transactional-email adapter for the enterprise EIP provider
 * execution boundary.
 *
 * Secrets arrive only through ExternalProviderExecutionContext. They are
 * never logged, persisted here, or copied into provider evidence.
 */
@Component
public final class BrevoEmailExternalProviderAdapter
        implements ExternalProviderAdapter {

    static final String CONNECTOR_TYPE = "BREVO_EMAIL";

    private static final Set<String> CONFIGURATION_FIELDS =
            Set.of(
                    "senderEmail",
                    "senderName"
            );

    private final ObjectMapper mapper;
    private final BrevoEmailTransport transport;

    @Autowired
    public BrevoEmailExternalProviderAdapter(
            ObjectMapper mapper
    ) {
        this(
                mapper,
                new SpringRestClientBrevoEmailTransport()
        );
    }

    BrevoEmailExternalProviderAdapter(
            ObjectMapper mapper,
            BrevoEmailTransport transport
    ) {
        this.mapper =
                Objects.requireNonNull(
                        mapper,
                        "mapper must not be null"
                );

        this.transport =
                Objects.requireNonNull(
                        transport,
                        "transport must not be null"
                );
    }

    @Override
    public String connectorType() {
        return CONNECTOR_TYPE;
    }

    @Override
    public ExternalProviderDispatchResult dispatch(
            ExternalProviderExecutionContext context,
            ExternalProviderDispatchRequest request
    ) {
        Objects.requireNonNull(
                context,
                "context must not be null"
        );

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        if (!"EMAIL".equalsIgnoreCase(request.channel())) {
            throw new IllegalArgumentException(
                    "BREVO_EMAIL connector supports EMAIL only"
            );
        }

        if (!"API_KEY".equalsIgnoreCase(context.authType())) {
            throw new IllegalStateException(
                    "BREVO_EMAIL connector requires API_KEY authentication"
            );
        }

        String apiKey =
                required(
                        context.credential(),
                        "Brevo API credential"
                );

        ProviderConfiguration configuration =
                providerConfiguration(
                        context.providerConfiguration()
                );

        URI endpoint =
                endpoint(
                        context.baseUrl()
                );

        Duration timeout =
                timeout(
                        context.requestTimeout()
                );

        String providerRequestBody =
                providerRequestBody(
                        configuration,
                        request
                );

        BrevoEmailTransportResponse response =
                transport.send(
                        endpoint,
                        apiKey,
                        providerRequestBody,
                        timeout
                );

        if (response.timedOut()) {
            return new ExternalProviderDispatchResult(
                    ExternalProviderDispatchResult.Status.TIMED_OUT,
                    null,
                    null,
                    "TIMEOUT",
                    "Brevo transactional email request timed out"
            );
        }

        int statusCode =
                response.statusCode();

        if (statusCode >= 200 && statusCode < 300) {
            String messageId =
                    messageId(
                            response.body()
                    );

            /*
             * A 2xx without a provider message id is delivery-ambiguous:
             * Brevo may already have accepted the message. Throwing here is
             * intentionally conservative because ENS does not cross-provider
             * fail over after an unclassified execution exception.
             */
            if (messageId == null) {
                throw new IllegalStateException(
                        "Brevo accepted transactional email without messageId"
                );
            }

            return new ExternalProviderDispatchResult(
                    ExternalProviderDispatchResult.Status.ACCEPTED,
                    messageId,
                    messageId,
                    "HTTP_" + statusCode,
                    "Brevo accepted transactional email"
            );
        }

        /*
         * Never expose the provider response body. Error payloads can include
         * operational details that do not belong in ENS evidence.
         */
        return new ExternalProviderDispatchResult(
                ExternalProviderDispatchResult.Status.FAILED,
                null,
                null,
                "HTTP_" + statusCode,
                "Brevo rejected transactional email"
        );
    }

    private String providerRequestBody(
            ProviderConfiguration configuration,
            ExternalProviderDispatchRequest request
    ) {
        String subject =
                required(
                        request.subject(),
                        "Email subject"
                );

        ObjectNode root =
                mapper.createObjectNode();

        ObjectNode sender =
                root.putObject(
                        "sender"
                );

        sender.put(
                "email",
                configuration.senderEmail()
        );

        if (configuration.senderName() != null) {
            sender.put(
                    "name",
                    configuration.senderName()
            );
        }

        root.putArray(
                "to"
        )
                .addObject()
                .put(
                        "email",
                        request.recipient()
                );

        root.put(
                "subject",
                subject
        );

        /*
         * Parent activation content is intentionally sent as text rather
         * than HTML. The secure activation body remains transient and is
         * never copied into ordinary ENS persistence.
         */
        root.put(
                "textContent",
                request.body()
        );

        if (
                request.idempotencyKey() != null
                && !request.idempotencyKey().isBlank()
        ) {
            root.putObject(
                    "headers"
            )
                    .put(
                            "Idempotency-Key",
                            request.idempotencyKey()
                    );
        }

        try {
            return mapper.writeValueAsString(
                    root
            );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to create Brevo transactional email request",
                    ex
            );
        }
    }

    private ProviderConfiguration providerConfiguration(
            String raw
    ) {
        String value =
                required(
                        raw,
                        "Brevo provider configuration"
                );

        try {
            JsonNode node =
                    mapper.readTree(
                            value
                    );

            if (
                    node == null
                    || !node.isObject()
            ) {
                throw new IllegalStateException(
                        "Brevo provider configuration must be a JSON object"
                );
            }

            Iterator<String> fields =
                    node.fieldNames();

            while (fields.hasNext()) {
                String field =
                        fields.next();

                if (!CONFIGURATION_FIELDS.contains(field)) {
                    throw new IllegalStateException(
                            "Unsupported Brevo provider configuration field: "
                                    + field
                    );
                }
            }

            String senderEmail =
                    text(
                            node,
                            "senderEmail"
                    );

            if (senderEmail == null) {
                throw new IllegalStateException(
                        "Brevo senderEmail is required"
                );
            }

            return new ProviderConfiguration(
                    senderEmail,
                    text(
                            node,
                            "senderName"
                    )
            );

        } catch (IllegalStateException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Brevo provider configuration is invalid",
                    ex
            );
        }
    }

    private String messageId(
            String responseBody
    ) {
        if (
                responseBody == null
                || responseBody.isBlank()
        ) {
            return null;
        }

        try {
            JsonNode response =
                    mapper.readTree(
                            responseBody
                    );

            return text(
                    response,
                    "messageId"
            );

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Brevo response could not be interpreted safely",
                    ex
            );
        }
    }

    private static URI endpoint(
            String baseUrl
    ) {
        URI base =
                URI.create(
                        required(
                                baseUrl,
                                "Brevo base URL"
                        )
                );

        /*
         * Provider credentials must never be transmitted over plaintext HTTP.
         */
        if (!"https".equalsIgnoreCase(base.getScheme())) {
            throw new IllegalStateException(
                    "BREVO_EMAIL connector requires an HTTPS base URL"
            );
        }

        if (
                base.getQuery() != null
                || base.getFragment() != null
        ) {
            throw new IllegalStateException(
                    "Brevo base URL must not contain query or fragment"
            );
        }

        String normalized =
                base.toString();

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return URI.create(
                normalized
                        + "/v3/smtp/email"
        );
    }

    private static Duration timeout(
            Duration timeout
    ) {
        if (
                timeout == null
                || timeout.isZero()
                || timeout.isNegative()
        ) {
            throw new IllegalStateException(
                    "Brevo request timeout must be positive"
            );
        }

        return timeout;
    }

    private static String text(
            JsonNode node,
            String field
    ) {
        JsonNode value =
                node.get(
                        field
                );

        if (
                value == null
                || value.isNull()
                || !value.isTextual()
        ) {
            return null;
        }

        String normalized =
                value.asText()
                        .trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private static String required(
            String value,
            String field
    ) {
        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalStateException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }

    private record ProviderConfiguration(
            String senderEmail,
            String senderName
    ) {
    }
}


@FunctionalInterface
interface BrevoEmailTransport {

    BrevoEmailTransportResponse send(
            URI endpoint,
            String apiKey,
            String requestBody,
            Duration timeout
    );
}


record BrevoEmailTransportResponse(
        int statusCode,
        String body,
        boolean timedOut
) {

    static BrevoEmailTransportResponse response(
            int statusCode,
            String body
    ) {
        return new BrevoEmailTransportResponse(
                statusCode,
                body,
                false
        );
    }

    static BrevoEmailTransportResponse timedOutResponse() {
        return new BrevoEmailTransportResponse(
                0,
                null,
                true
        );
    }
}


final class SpringRestClientBrevoEmailTransport
        implements BrevoEmailTransport {

    @Override
    public BrevoEmailTransportResponse send(
            URI endpoint,
            String apiKey,
            String requestBody,
            Duration timeout
    ) {
        HttpClient httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                timeout
                        )
                        .followRedirects(
                                HttpClient.Redirect.NEVER
                        )
                        .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(
                        httpClient
                );

        requestFactory.setReadTimeout(
                timeout
        );

        RestClient client =
                RestClient.builder()
                        .requestFactory(
                                requestFactory
                        )
                        .build();

        try {
            return client
                    .post()
                    .uri(
                            endpoint
                    )
                    .accept(
                            MediaType.APPLICATION_JSON
                    )
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .header(
                            "api-key",
                            apiKey
                    )
                    .body(
                            requestBody
                    )
                    .exchangeForRequiredValue(
                            (request, response) ->
                                    BrevoEmailTransportResponse.response(
                                            response
                                                    .getStatusCode()
                                                    .value(),
                                            new String(
                                                    response
                                                            .getBody()
                                                            .readAllBytes(),
                                                    StandardCharsets.UTF_8
                                            )
                                    )
                    );

        } catch (ResourceAccessException ex) {
            if (isTimeout(ex)) {
                return BrevoEmailTransportResponse
                        .timedOutResponse();
            }

            /*
             * Other I/O failures are deliberately left delivery-ambiguous.
             * ENS will record a safe execution failure and will not
             * immediately cross-provider fail over.
             */
            throw new IllegalStateException(
                    "Brevo email transport failed",
                    ex
            );
        }
    }

    private static boolean isTimeout(
            Throwable error
    ) {
        Throwable current =
                error;

        while (current != null) {
            if (
                    current instanceof HttpTimeoutException
                    || current instanceof SocketTimeoutException
            ) {
                return true;
            }

            current =
                    current.getCause();
        }

        return false;
    }
}
