package africa.growtogether.platform.eiam.audit;
import africa.growtogether.platform.common.api.*; import africa.growtogether.platform.common.web.RequestContextHolder;
import java.time.Instant; import java.time.temporal.ChronoUnit; import java.util.UUID;
import org.springframework.data.domain.Page; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/eiam/audit-events")
public class AuditEventController {
 private final AuditEventService service; private final ApiResponses responses;
 public AuditEventController(AuditEventService service,ApiResponses responses){this.service=service;this.responses=responses;}
 @GetMapping @PreAuthorize("hasAuthority('eiam.audit.read')")
 public ApiResponse<Page<AuditEventView>> search(@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,@RequestParam(required=false) String eventType,@RequestParam(required=false) UUID actorUserId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size){
  UUID tenantId=UUID.fromString(RequestContextHolder.require().tenantId()); Instant upper=to==null?Instant.now():to; Instant lower=from==null?upper.minus(30,ChronoUnit.DAYS):from;
  if(lower.isAfter(upper))throw new IllegalArgumentException("from must not be after to");
  return responses.success("GT-EIAM-AUDIT-001","Audit events retrieved.",service.search(tenantId,lower,upper,eventType,actorUserId,page,size));
 }
}
