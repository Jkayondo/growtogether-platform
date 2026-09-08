package africa.growtogether.platform.eip;

import africa.growtogether.platform.eaif.execution.AiTextRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenAiProviderAdapterTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private ExternalProviderExecutionContext context(String endpoint) {
        return new ExternalProviderExecutionContext(UUID.randomUUID(), "TEST", "OPENAI_RESPONSES",
                endpoint, "API_KEY", "test-secret", null, Duration.ofSeconds(5));
    }
    private AiTextRequest request() { return new AiTextRequest("configured-model", "private evidence", 200); }
    private String response(String status, String content) {
        return "{\"id\":\"resp_test\",\"status\":\"" + status
                + "\",\"output\":[{\"type\":\"reasoning\"},{\"type\":\"message\","
                + "\"role\":\"assistant\",\"status\":\"completed\",\"content\":" + content + "}]}";
    }
    @Test void serializesConfiguredModelAndExtractsAllTextItems() {
        var adapter = new OpenAiProviderAdapter(mapper, (key, body, timeout) -> {
            var json = mapper.readTree(body);
            assertEquals("configured-model", json.path("model").asText());
            assertEquals("private evidence", json.path("input").asText());
            assertFalse(json.path("store").asBoolean());
            assertFalse(json.path("stream").asBoolean());
            assertEquals(200, json.path("max_output_tokens").asInt());
            assertEquals("test-secret", key);
            return new OpenAiProviderAdapter.Reply(200, response("completed",
                    "[{\"type\":\"output_text\",\"text\":\"First\"},{\"type\":\"output_text\",\"text\":\"Second\"}]"));
        });
        var result = adapter.execute(context("https://api.openai.com/v1"), request());
        assertEquals("First\nSecond", result.text());
        assertEquals("resp_test", result.providerResponseId());
        assertFalse(result.toString().contains("First"));
    }
    @Test void refusesUnapprovedHostBeforeSendingCredential() {
        var adapter = new OpenAiProviderAdapter(mapper, (a,b,c) -> { fail("Must not send"); return null; });
        for (String endpoint : new String[]{"http://api.openai.com", "https://api.openai.com.evil.test",
                "https://api.openai.com@evil.test", "https://api.openai.com/?redirect=evil"})
            assertThrows(IllegalStateException.class, () -> adapter.execute(context(endpoint), request()));
    }
    @Test void failsClosedForErrorsIncompleteRefusalEmptyAndMalformedResponses() {
        String[] bodies = {"not json", response("incomplete", "[]"), response("completed", "[]"),
                response("completed", "[{\"type\":\"refusal\",\"refusal\":\"private reason\"}]")};
        for (String body : bodies) {
            var adapter = new OpenAiProviderAdapter(mapper, (a,b,c) -> new OpenAiProviderAdapter.Reply(200, body));
            assertThrows(IllegalStateException.class, () -> adapter.execute(context("https://api.openai.com"), request()));
        }
    }
    @Test void doesNotExposeProviderErrorOrRetry() {
        int[] attempts = {0};
        var adapter = new OpenAiProviderAdapter(mapper, (a,b,c) -> {
            attempts[0]++;
            return new OpenAiProviderAdapter.Reply(429, "test-secret private evidence");
        });
        var error = assertThrows(IllegalStateException.class,
                () -> adapter.execute(context("https://api.openai.com"), request()));
        assertEquals(1, attempts[0]);
        assertNull(error.getCause());
        assertFalse(error.getMessage().contains("test-secret"));
    }
    @Test void sanitizesTransportTimeout() {
        var adapter = new OpenAiProviderAdapter(mapper, (a,b,c) -> {
            throw new java.net.http.HttpTimeoutException("test-secret");
        });
        var error = assertThrows(IllegalStateException.class,
                () -> adapter.execute(context("https://api.openai.com"), request()));
        assertNull(error.getCause());
        assertFalse(error.getMessage().contains("test-secret"));
    }
}
