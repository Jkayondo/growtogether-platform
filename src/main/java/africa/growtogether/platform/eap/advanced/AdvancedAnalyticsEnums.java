package africa.growtogether.platform.eap.advanced;
public final class AdvancedAnalyticsEnums { private AdvancedAnalyticsEnums(){}
 public enum AlertOperator { GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL }
 public enum AlertSeverity { INFO, LOW, MEDIUM, HIGH, CRITICAL }
 public enum AlertStatus { ACTIVE, ACKNOWLEDGED, RESOLVED, SUPPRESSED }
 public enum TrendDirection { RISING, FALLING, STABLE, INSUFFICIENT_DATA }
 public enum ScheduleStatus { ACTIVE, PAUSED, DISABLED }
 public enum DeliveryChannel { EMAIL, IN_APP, WEBHOOK, DOCUMENT }
 public enum DataSourceType { PLATFORM_EVENT, SERVICE_API, DATABASE_VIEW, FILE_IMPORT, EXTERNAL_CONNECTOR }
}
