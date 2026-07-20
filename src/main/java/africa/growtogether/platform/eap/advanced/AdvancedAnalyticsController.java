package africa.growtogether.platform.eap.advanced;
import jakarta.validation.Valid; import org.springframework.format.annotation.DateTimeFormat; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api/v1/analytics/advanced") public class AdvancedAnalyticsController {
 private final AdvancedAnalyticsService service; public AdvancedAnalyticsController(AdvancedAnalyticsService service){this.service=service;}
 @PostMapping("/alerts/rules") @PreAuthorize("hasAuthority('analytics.alert.manage')") public Map<String,Object> createRule(@Valid @RequestBody AdvancedAnalyticsDtos.CreateAlertRule r){return Map.of("id",service.createAlertRule(r));}
 @PostMapping("/alerts/evaluate") @PreAuthorize("hasAuthority('analytics.alert.evaluate')") public Map<String,Object> evaluate(@Valid @RequestBody AdvancedAnalyticsDtos.EvaluateAlerts r){return Map.of("alertIds",service.evaluate(r));}
 @PostMapping("/alerts/{id}/acknowledge") @PreAuthorize("hasAuthority('analytics.alert.manage')") public void acknowledge(@PathVariable UUID id){service.acknowledge(id);} @PostMapping("/alerts/{id}/resolve") @PreAuthorize("hasAuthority('analytics.alert.manage')") public void resolve(@PathVariable UUID id){service.resolve(id);}
 @PostMapping("/reports/schedules") @PreAuthorize("hasAuthority('analytics.schedule.manage')") public Map<String,Object> schedule(@Valid @RequestBody AdvancedAnalyticsDtos.CreateSchedule r){return Map.of("id",service.createSchedule(r));}
 @PostMapping("/reports/schedules/run-due") @PreAuthorize("hasAuthority('analytics.schedule.execute')") public Map<String,Object> runDue(){return Map.of("executed",service.runDueSchedules());}
 @PostMapping("/data-sources") @PreAuthorize("hasAuthority('analytics.datasource.manage')") public Map<String,Object> source(@Valid @RequestBody AdvancedAnalyticsDtos.CreateDataSource r){return Map.of("id",service.createDataSource(r));}
 @GetMapping("/trends/{metricCode}") @PreAuthorize("hasAuthority('analytics.read')") public AdvancedAnalyticsDtos.TrendResult trend(@PathVariable String metricCode,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to){return service.trend(metricCode,from,to);}
}
