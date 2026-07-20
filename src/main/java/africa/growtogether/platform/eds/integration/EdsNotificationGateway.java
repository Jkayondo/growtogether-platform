package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.ens.*;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** ENS adapter. EDS publishes notification intent and never calls providers directly. */
@Component
public class EdsNotificationGateway {
    private final NotificationService notifications;
    public EdsNotificationGateway(NotificationService notifications) { this.notifications = notifications; }
    public UUID documentShared(UUID documentId, String recipient, String title) {
        return notifications.send(new SendCommand("EDS_DOCUMENT_SHARED", recipient, NotificationChannel.IN_APP,
            NotificationPriority.NORMAL, "Document shared", "A document has been shared: " + title,
            "EDS", documentId.toString())).id();
    }
    public UUID legalHoldApplied(UUID documentId, String recipient, String title) {
        return notifications.send(new SendCommand("EDS_LEGAL_HOLD_APPLIED", recipient, NotificationChannel.IN_APP,
            NotificationPriority.HIGH, "Legal hold applied", "A legal hold was applied to: " + title,
            "EDS", documentId.toString())).id();
    }
}
