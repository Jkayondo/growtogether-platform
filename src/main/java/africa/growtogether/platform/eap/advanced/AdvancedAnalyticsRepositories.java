package africa.growtogether.platform.eap.advanced;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.Instant; import java.util.*;
interface AlertRuleRepository extends JpaRepository<AlertRule,UUID>{List<AlertRule> findByTenantIdAndMetricCodeAndActiveTrue(UUID tenant,String metric);Optional<AlertRule> findByTenantIdAndRuleCode(UUID tenant,String code);}
interface AnalyticsAlertRepository extends JpaRepository<AnalyticsAlert,UUID>{List<AnalyticsAlert> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenant,AdvancedAnalyticsEnums.AlertStatus status);}
interface ReportScheduleRepository extends JpaRepository<ReportSchedule,UUID>{List<ReportSchedule> findTop100ByTenantIdAndStatusAndNextRunAtLessThanEqualOrderByNextRunAt(UUID tenant,AdvancedAnalyticsEnums.ScheduleStatus status,Instant now);}
interface AnalyticsDataSourceRepository extends JpaRepository<AnalyticsDataSource,UUID>{Optional<AnalyticsDataSource> findByTenantIdAndSourceCode(UUID tenant,String code);}
