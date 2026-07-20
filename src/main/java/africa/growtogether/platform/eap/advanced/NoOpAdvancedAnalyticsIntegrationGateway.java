package africa.growtogether.platform.eap.advanced;
import org.springframework.stereotype.Component;
@Component public class NoOpAdvancedAnalyticsIntegrationGateway implements AdvancedAnalyticsIntegrationGateway {
 public void deliverScheduledReport(java.util.UUID tenantId,java.util.UUID executionId,String reportCode,AdvancedAnalyticsEnums.DeliveryChannel channel,String recipientsJson){}
 public void publishAlert(java.util.UUID tenantId,java.util.UUID alertId,String metricCode,AdvancedAnalyticsEnums.AlertSeverity severity){}
}
