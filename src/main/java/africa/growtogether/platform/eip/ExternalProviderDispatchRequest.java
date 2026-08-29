package africa.growtogether.platform.eip;

import java.util.Map;

public record ExternalProviderDispatchRequest(

        String channel,

        String recipient,

        String subject,

        String body,

        String correlationId,

        String idempotencyKey,

        Map<String, String> attributes

) {

    public ExternalProviderDispatchRequest {

        channel = required(
                channel,
                "channel"
        );

        recipient = required(
                recipient,
                "recipient"
        );

        body = required(
                body,
                "body"
        );

        subject =
                subject == null
                        ? null
                        : subject.trim();

        correlationId =
                correlationId == null
                        ? null
                        : correlationId.trim();

        idempotencyKey =
                idempotencyKey == null
                        ? null
                        : idempotencyKey.trim();

        attributes =
                attributes == null
                        ? Map.of()
                        : Map.copyOf(attributes);
    }

    private static String required(
            String value,
            String field
    ) {
        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }
}
