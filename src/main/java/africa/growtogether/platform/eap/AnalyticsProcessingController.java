package africa.growtogether.platform.eap;
import jakarta.validation.Valid; import org.springframework.format.annotation.DateTimeFormat; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api/v1/analytics")
public class AnalyticsProcessingController {
 private final AnalyticsProcessingService service; public AnalyticsProcessingController(AnalyticsProcessingService service){this.service=service;}
 @PostMapping("/events") @PreAuthorize("hasAuthority('analytics.event.ingest')") public Map<String,Object> ingest(@Valid @RequestBody EapDtos.IngestEvent r){return Map.of("id",service.ingest(r));}
 @PostMapping("/metrics") @PreAuthorize("hasAuthority('analytics.metric.manage')") public Map<String,Object> metric(@Valid @RequestBody EapDtos.CreateMetric r){return Map.of("id",service.createMetric(r));}
 @PostMapping("/processing/run") @PreAuthorize("hasAuthority('analytics.processing.manage')") public Map<String,Object> process(@RequestParam(defaultValue="5") int maxAttempts){return Map.of("processed",service.processReceived(maxAttempts));}
 @GetMapping("/metrics/{code}/series") @PreAuthorize("hasAuthority('analytics.read')") public List<EapDtos.MetricPoint> series(@PathVariable String code,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to){return service.metric(code,from,to);}
 @PostMapping("/dashboards") @PreAuthorize("hasAuthority('analytics.dashboard.manage')") public Map<String,Object> dashboard(@Valid @RequestBody EapDtos.CreateDashboard r){return Map.of("id",service.createDashboard(r));}
 @GetMapping("/dashboards/{code}/runtime") @PreAuthorize("hasAuthority('analytics.read')") public EapDtos.DashboardRuntime dashboardRuntime(@PathVariable String code,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to){return service.dashboard(code,from,to);}
 @PostMapping("/reports/executions") @PreAuthorize("hasAuthority('analytics.report.execute')") public EapDtos.ReportResult report(@Valid @RequestBody EapDtos.RequestReport r){return service.requestReport(r);}
}
