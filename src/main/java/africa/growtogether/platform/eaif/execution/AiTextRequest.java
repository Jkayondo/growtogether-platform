package africa.growtogether.platform.eaif.execution;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Exact UTF-8 input is bound to the hash of the governed EAIF request. */
public record AiTextRequest(String model, String input, int maxOutputTokens) {
    public AiTextRequest {
        if (model == null || model.isBlank() || model.length() > 180)
            throw new IllegalArgumentException("A configured model is required");
        if (input == null || input.isBlank() || input.length() > 100000)
            throw new IllegalArgumentException("Input must contain 1 to 100000 characters");
        if (maxOutputTokens < 1 || maxOutputTokens > 16384)
            throw new IllegalArgumentException("Invalid output token limit");
    }

    public static String hash(String input) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable");
        }
    }

    @Override public String toString() { return "AiTextRequest[content redacted]"; }
}
