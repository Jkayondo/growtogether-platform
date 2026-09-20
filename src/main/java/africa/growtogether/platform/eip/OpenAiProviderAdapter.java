package africa.growtogether.platform.eip;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.execution.AiTextRequest;
import africa.growtogether.platform.eaif.execution.AiTextResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** OpenAI-specific details stop here. No SDK/provider DTO leaks into product code. */
@Component
public final class OpenAiProviderAdapter implements AiProviderAdapter {
    private final ObjectMapper mapper;
    private final Transport transport;
    @Autowired public OpenAiProviderAdapter(ObjectMapper mapper) { this(mapper, new JdkTransport()); }
    OpenAiProviderAdapter(ObjectMapper mapper, Transport transport) {
        this.mapper = mapper; this.transport = transport;
    }
    public String connectorType() { return "OPENAI_RESPONSES"; }
    public AiEnums.ProviderType providerType() { return AiEnums.ProviderType.OPENAI_COMPATIBLE; }

    public AiTextResult execute(ExternalProviderExecutionContext context, AiTextRequest request) {
        // This adapter supports the official OpenAI endpoint only. Other endpoints require another adapter.
        if (!("https://api.openai.com".equals(context.baseUrl())
                || "https://api.openai.com/".equals(context.baseUrl())
                || "https://api.openai.com/v1".equals(context.baseUrl())
                || "https://api.openai.com/v1/".equals(context.baseUrl())))
            throw new IllegalStateException("Unsupported OpenAI endpoint");
        if (!"API_KEY".equals(context.authType()) || context.credential() == null
                || context.credential().isBlank())
            throw new IllegalStateException("OpenAI API_KEY connector configuration required");
        if (context.requestTimeout() == null || context.requestTimeout().isNegative()
                || context.requestTimeout().isZero()) throw new IllegalStateException("Invalid AI timeout");
        boolean providerResponseReceived = false;

        try {
            var body = mapper.createObjectNode().put("model", request.model())
                    .put("input", request.input()).put("max_output_tokens", request.maxOutputTokens())
                    .put("store", false).put("stream", false);
            String requestBody;
            try {
                requestBody = mapper.writeValueAsString(body);
            } catch (Exception ex) {
                throw new AiProviderFailureException(
                        "AI_PROVIDER_REQUEST_SERIALIZATION_FAILED"
                );
            }

            Reply reply = transport.send(
                    context.credential(),
                    requestBody,
                    context.requestTimeout()
            );

            providerResponseReceived = true;

            if (reply.status() < 200 || reply.status() >= 300)
                throw new AiProviderFailureException(
                        "AI_PROVIDER_HTTP_" + reply.status()
                );
            var root = mapper.readTree(reply.body());
            if (root == null || !"completed".equals(root.path("status").asText())
                    || !root.path("error").isNull() && !root.path("error").isMissingNode())
                throw new AiProviderFailureException("AI_PROVIDER_NOT_COMPLETED");
            StringBuilder text = new StringBuilder();
            for (var item : root.path("output")) {
                if (!"message".equals(item.path("type").asText())) continue;
                if (!"assistant".equals(item.path("role").asText())
                        || !"completed".equals(item.path("status").asText()))
                    throw new AiProviderFailureException("AI_PROVIDER_INVALID_MESSAGE");
                for (var part : item.path("content")) {
                    if ("refusal".equals(part.path("type").asText()))
                        throw new AiProviderFailureException("AI_PROVIDER_REFUSAL");
                    if ("output_text".equals(part.path("type").asText())) {
                        if (!part.path("text").isTextual()) throw new AiProviderFailureException("AI_PROVIDER_INVALID_TEXT");
                        if (!text.isEmpty()) text.append('\n');
                        text.append(part.path("text").asText());
                    }
                }
            }
            return new AiTextResult(root.path("id").asText(), text.toString());
        } catch (AiProviderFailureException ex) {
            throw ex;

        } catch (java.net.http.HttpTimeoutException ex) {
            throw new AiProviderFailureException(
                    "AI_PROVIDER_TIMEOUT"
            );

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiProviderFailureException(
                    "AI_PROVIDER_INTERRUPTED"
            );

        } catch (Exception ex) {
            /*
             * No raw response body, credential, prompt, endpoint or nested
             * transport exception escapes this boundary.
             *
             * We retain only whether failure happened before or after a
             * provider response was received.
             */
            throw new AiProviderFailureException(
                    providerResponseReceived
                            ? "AI_PROVIDER_RESPONSE_INVALID"
                            : "AI_PROVIDER_TRANSPORT_FAILURE"
            );
        }
    }

    interface Transport { Reply send(String credential, String body, Duration timeout) throws Exception; }
    record Reply(int status, String body) {
        @Override public String toString() { return "Reply[body redacted]"; }
    }
    static final class JdkTransport implements Transport {
        public Reply send(String credential, String body, Duration timeout) throws Exception {
            HttpClient client = HttpClient.newBuilder().connectTimeout(timeout)
                    .followRedirects(HttpClient.Redirect.NEVER).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/responses"))
                    .timeout(timeout).header("Authorization", "Bearer " + credential)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
            try {
                var response = client.send(request, info -> new LimitedBodySubscriber());
                return new Reply(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw ex;
            }
        }
    }
    static final class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
        private Flow.Subscription subscription;
        private long size;
        private boolean stopped;
        public CompletionStage<byte[]> getBody() { return delegate.getBody(); }
        public void onSubscribe(Flow.Subscription value) { subscription = value; delegate.onSubscribe(value); }
        public void onNext(List<ByteBuffer> buffers) {
            if (stopped) return;
            for (ByteBuffer buffer : buffers) size += buffer.remaining();
            if (size > 2000000) {
                stopped = true;
                subscription.cancel();
                delegate.onError(new IllegalStateException("AI response too large"));
            } else delegate.onNext(buffers);
        }
        public void onError(Throwable error) { if (!stopped) { stopped = true; delegate.onError(error); } }
        public void onComplete() { if (!stopped) { stopped = true; delegate.onComplete(); } }
    }
}
