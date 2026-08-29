package africa.growtogether.platform.ens;

import java.util.UUID;

public record NotificationDispatchWorkItem(

        UUID notificationId,

        UUID tenantId,

        String definitionCode,

        String recipient,

        NotificationChannel channel,

        NotificationPriority priority,

        String subject,

        String body,

        String correlationId,

        String sourceService,

        String sourceReference,

        int processingAttempt

) {
}
