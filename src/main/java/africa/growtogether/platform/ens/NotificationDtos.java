package africa.growtogether.platform.ens;
import jakarta.validation.constraints.*; import java.time.Instant; import java.util.UUID;
public final class NotificationDtos { private NotificationDtos(){}
 public record SendCommand(@NotBlank String definitionCode,@NotBlank String recipient,@NotNull NotificationChannel channel,NotificationPriority priority,String subject,@NotBlank String body,String sourceService,String sourceReference){}
 public record View(UUID id,UUID tenantId,String definitionCode,String recipient,NotificationChannel channel,NotificationPriority priority,NotificationStatus status,String subject,String sourceService,String sourceReference,int attemptCount,Instant nextAttemptAt,String providerReference,String lastError){}
}
