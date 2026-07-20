package africa.growtogether.platform.eap;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
public final class EapDtos { private EapDtos(){}
 public record IngestEvent(@NotBlank String eventType,@NotBlank String sourceService,String correlationId,Instant eventTime,@NotBlank String payloadJson){}
 public record CreateMetric(@NotBlank String code,@NotBlank String name,@NotBlank String eventType,AnalyticsEnums.MetricType type,AnalyticsEnums.AggregationPeriod period,String valuePath,String dimensionPathsJson){}
 public record MetricPoint(Instant bucketStart,long sampleCount,BigDecimal sumValue,BigDecimal average){}
 public record CreateDashboard(@NotBlank String code,@NotBlank String name,@NotBlank String widgetConfigurationJson){}
 public record DashboardRuntime(String code,String widgetConfigurationJson,Map<String,List<MetricPoint>> data){}
 public record RequestReport(@NotBlank String reportCode,String parametersJson,AnalyticsEnums.ExportFormat format){}
 public record ReportResult(UUID id,AnalyticsEnums.ReportStatus status,String artifactReference){}
}
