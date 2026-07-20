package africa.growtogether.platform.eap.integration;

import africa.growtogether.platform.eap.advanced.*;
import africa.growtogether.platform.eip.integration.PlatformIntegrationGateway;
import africa.growtogether.platform.ens.*;
import africa.growtogether.platform.ens.NotificationDtos.SendCommand;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Shared integration contract for ENS, EIP and future EDS report artifacts. */
@Primary
@Component
public class EapEnterpriseIntegrationGateway implements AdvancedAnalyticsIntegrationGateway {
    private final NotificationService notifications;
    private final PlatformIntegrationGateway integration;
    private final EapConfigurationGateway configuration;

    public EapEnterpriseIntegrationGateway(NotificationService notifications,
                                            PlatformIntegrationGateway integration,
                                            EapConfigurationGateway configuration) {
        this.notifications = notifications;
        this.integration = integration;
        this.configuration = configuration;
    }

    @Override
    public void deliverScheduledReport(UUID tenantId, UUID executionId, String reportCode,
                                       AdvancedAnalyticsEnums.DeliveryChannel channel, String recipientsJson) {
        integration.publish("AnalyticsReportScheduled", "EAP", target(channel),
            "{\"executionId\":\"" + executionId + "\",\"reportCode\":\"" + escape(reportCode) + "\"}",
            executionId.toString(), "eap-report-" + executionId);
        if (channel == AdvancedAnalyticsEnums.DeliveryChannel.EMAIL || channel == AdvancedAnalyticsEnums.DeliveryChannel.IN_APP) {
            notifications.send(new SendCommand("ANALYTICS_REPORT_READY", normalizeRecipient(recipientsJson),
                channel == AdvancedAnalyticsEnums.DeliveryChannel.EMAIL ? NotificationChannel.EMAIL : NotificationChannel.IN_APP,
                NotificationPriority.NORMAL, "Analytics report ready",
                "Report " + reportCode + " has been scheduled for delivery.", "EAP", executionId.toString()));
        }
    }

    @Override
    public void publishAlert(UUID tenantId, UUID alertId, String metricCode, AdvancedAnalyticsEnums.AlertSeverity severity) {
        integration.publish("AnalyticsAlertTriggered", "EAP", "ENS",
            "{\"alertId\":\"" + alertId + "\",\"metricCode\":\"" + escape(metricCode) + "\",\"severity\":\"" + severity + "\"}",
            alertId.toString(), "eap-alert-" + alertId);
        if (configuration.alertDeliveryEnabled()) {
            notifications.send(new SendCommand("ANALYTICS_ALERT", "analytics-administrators", NotificationChannel.IN_APP,
                priority(severity), "Analytics alert: " + metricCode,
                "Metric " + metricCode + " triggered a " + severity + " alert.", "EAP", alertId.toString()));
        }
    }

    private static String target(AdvancedAnalyticsEnums.DeliveryChannel channel) {
        return channel == AdvancedAnalyticsEnums.DeliveryChannel.WEBHOOK ? "EXTERNAL_WEBHOOK" :
               channel == AdvancedAnalyticsEnums.DeliveryChannel.DOCUMENT ? "EDS" : "ENS";
    }
    private static NotificationPriority priority(AdvancedAnalyticsEnums.AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> NotificationPriority.CRITICAL;
            case HIGH -> NotificationPriority.HIGH;
            default -> NotificationPriority.NORMAL;
        };
    }
    private static String normalizeRecipient(String value) {
        if (value == null || value.isBlank()) return "analytics-administrators";
        return value.length() > 250 ? value.substring(0, 250) : value;
    }
    private static String escape(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
