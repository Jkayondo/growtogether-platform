package africa.growtogether.platform.eip;
import jakarta.validation.constraints.*; import java.time.Instant; import java.util.UUID;
public final class IntegrationDtos { private IntegrationDtos(){}
 public record PublishCommand(@NotBlank String eventType,@NotBlank String eventVersion,@NotBlank String sourceService,@NotBlank String destination,IntegrationProtocol protocol,@NotBlank String payload,String headersJson,String correlationId,@NotBlank String idempotencyKey,@Min(1) @Max(20) int maxAttempts){}
 public record FailCommand(@NotBlank String error,@Min(1) long retryAfterSeconds){}
 public record RouteCommand(@NotBlank String routeCode,@NotBlank String eventPattern,@NotBlank String destination,@NotNull IntegrationProtocol protocol,int priority){}
 public record MessageView(UUID id,String eventType,String destination,IntegrationProtocol protocol,IntegrationMessageStatus status,int attempts,int maxAttempts,Instant nextAttemptAt,Instant deliveredAt,String lastError,UUID replayOfMessageId){static MessageView from(IntegrationMessage m){return new MessageView(m.id(),m.eventType(),m.destination(),m.protocol(),m.messageStatus(),m.attemptCount(),m.maxAttempts(),m.nextAttemptAt(),m.deliveredAt(),m.lastError(),m.replayOfMessageId());}}
}
