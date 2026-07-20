package africa.growtogether.platform.eap.advanced;
public interface AdvancedAnalyticsIntegrationGateway {
 void deliverScheduledReport(java.util.UUID tenantId,java.util.UUID executionId,String reportCode,AdvancedAnalyticsEnums.DeliveryChannel channel,String recipientsJson);
 void publishAlert(java.util.UUID tenantId,java.util.UUID alertId,String metricCode,AdvancedAnalyticsEnums.AlertSeverity severity);
}
