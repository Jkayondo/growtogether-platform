package africa.growtogether.platform.eap;

public final class AnalyticsEnums {
 private AnalyticsEnums() {}
 public enum EventStatus { RECEIVED, PROCESSING, PROCESSED, FAILED, DEAD_LETTER }
 public enum MetricType { COUNTER, SUM, AVERAGE, MINIMUM, MAXIMUM, DISTINCT_COUNT, RATE }
 public enum AggregationPeriod { MINUTE, HOUR, DAY, WEEK, MONTH }
 public enum ReportStatus { QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED }
 public enum ExportFormat { JSON, CSV, PDF, XLSX }
}
