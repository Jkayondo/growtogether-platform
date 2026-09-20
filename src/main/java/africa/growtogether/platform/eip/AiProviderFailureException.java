package africa.growtogether.platform.eip;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Carries only a bounded, sanitized provider failure category.
 *
 * The exception message deliberately remains generic so provider bodies,
 * prompts, credentials, endpoint details and nested transport exceptions
 * cannot escape through the product/API error boundary.
 */
public final class AiProviderFailureException extends IllegalStateException {

    private static final String GENERIC_MESSAGE =
            "AI provider execution did not produce a complete text result";

    private static final Pattern HTTP_CATEGORY =
            Pattern.compile("AI_PROVIDER_HTTP_[1-5][0-9][0-9]");

    private static final Set<String> FIXED_CATEGORIES =
            Set.of(
                    "AI_PROVIDER_NOT_COMPLETED",
                    "AI_PROVIDER_INVALID_MESSAGE",
                    "AI_PROVIDER_REFUSAL",
                    "AI_PROVIDER_INVALID_TEXT",
                    "AI_PROVIDER_RESPONSE_INVALID",
                    "AI_PROVIDER_TIMEOUT",
                    "AI_PROVIDER_INTERRUPTED",
                    "AI_PROVIDER_TRANSPORT_FAILURE",
                    "AI_PROVIDER_REQUEST_SERIALIZATION_FAILED"
            );

    private final String category;

    public AiProviderFailureException(String category) {
        super(GENERIC_MESSAGE);
        this.category = requireSafeCategory(category);
    }

    public String category() {
        return category;
    }

    private static String requireSafeCategory(String category) {

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "AI provider failure category is required"
            );
        }

        String normalized = category.trim();

        if (HTTP_CATEGORY.matcher(normalized).matches()
                || FIXED_CATEGORIES.contains(normalized)) {
            return normalized;
        }

        throw new IllegalArgumentException(
                "Unsupported AI provider failure category"
        );
    }
}
