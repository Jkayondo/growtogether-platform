package africa.growtogether.platform.eap.advanced;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
public final class AdvancedAnalyticsDtos {private AdvancedAnalyticsDtos(){}
 public record CreateAlertRule(@NotBlank String code,@NotBlank String name,@NotBlank String metricCode,AdvancedAnalyticsEnums.AlertOperator operator,@NotNull BigDecimal threshold,AdvancedAnalyticsEnums.AlertSeverity severity,long cooldownSeconds){}
 public record CreateSchedule(@NotBlank String code,@NotBlank String reportCode,@NotBlank String cronExpression,@NotBlank String timezone,AdvancedAnalyticsEnums.DeliveryChannel deliveryChannel,@NotBlank String recipientsJson,String parametersJson,Instant nextRunAt){}
 public record CreateDataSource(@NotBlank String code,@NotBlank String name,AdvancedAnalyticsEnums.DataSourceType type,String sourceService,String configurationReference,String schemaJson){}
 public record TrendResult(String metricCode,Instant from,Instant to,AdvancedAnalyticsEnums.TrendDirection direction,BigDecimal change,BigDecimal percentageChange,int points){}
 public record EvaluateAlerts(String metricCode,BigDecimal observedValue){}
}
