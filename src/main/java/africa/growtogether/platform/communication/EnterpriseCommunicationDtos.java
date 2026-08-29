package africa.growtogether.platform.communication;

import africa.growtogether.platform.connect.ConnectMessage;
import africa.growtogether.platform.ens.NotificationChannel;
import africa.growtogether.platform.ens.NotificationDtos;
import africa.growtogether.platform.ens.NotificationPriority;

import java.util.UUID;

public final class EnterpriseCommunicationDtos {

    private EnterpriseCommunicationDtos() {
    }

    public record ConnectIntent(
            UUID spaceId,
            String body
    ) {
    }

    public record NotificationIntent(
            String definitionCode,
            String recipient,
            NotificationChannel channel,
            NotificationPriority priority,
            String subject,
            String body
    ) {
    }

    public record DeliveryCommand(
            UUID tenantId,
            String sourceService,
            String sourceReference,
            String correlationId,
            ConnectIntent connect,
            NotificationIntent notification
    ) {
    }

    public record DeliveryResult(
            ConnectMessage connectMessage,
            NotificationDtos.View notification,
            String correlationId
    ) {
    }
}
