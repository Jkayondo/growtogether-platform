package africa.growtogether.platform.eiam.audit;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.web.RequestContextHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock; import java.time.Instant; import java.util.Map; import java.util.UUID;
import org.springframework.data.domain.Page; import org.springframework.data.domain.PageRequest; import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Propagation; import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventService {
 private final AuditEventRepository repository; private final ObjectMapper mapper; private final HttpServletRequest request; private final Clock clock=Clock.systemUTC();
 public AuditEventService(AuditEventRepository repository,ObjectMapper mapper,HttpServletRequest request){this.repository=repository;this.mapper=mapper;this.request=request;}
 @Transactional(propagation=Propagation.REQUIRES_NEW)
 public AuditEventView record(RecordAuditEventCommand command){
  var context=RequestContextHolder.require(); UUID tenantId=UUID.fromString(context.tenantId());
  var auth=SecurityContextHolder.getContext().getAuthentication(); GtPrincipal principal=auth!=null&&auth.getPrincipal() instanceof GtPrincipal p?p:null;
  AuditEvent event=new AuditEvent(tenantId,principal==null?null:principal.userId(),principal==null?"anonymous":principal.username(),required(command.eventType()),required(command.category()),required(command.outcome()),required(command.severity()),command.resourceType(),command.resourceId(),clientIp(),truncate(request.getHeader("User-Agent"),512),context.correlationId(),principal==null?null:principal.sessionId(),required(command.message()),json(command.details()),Instant.now(clock));
  return AuditEventView.from(repository.save(event));
 }
 @Transactional(readOnly=true)
 public Page<AuditEventView> search(UUID tenantId, Instant from, Instant to, String eventType, UUID actorUserId, int page, int size){
  int safeSize=Math.min(Math.max(size,1),100); var pageable=PageRequest.of(Math.max(page,0),safeSize,Sort.by(Sort.Direction.DESC,"occurredAt"));
  Page<AuditEvent> result= eventType!=null&&!eventType.isBlank()?repository.findByTenantIdAndEventTypeAndOccurredAtBetween(tenantId,eventType.trim(),from,to,pageable):actorUserId!=null?repository.findByTenantIdAndActorUserIdAndOccurredAtBetween(tenantId,actorUserId,from,to,pageable):repository.findByTenantIdAndOccurredAtBetween(tenantId,from,to,pageable);
  return result.map(AuditEventView::from);
 }
 private String clientIp(){String forwarded=request.getHeader("X-Forwarded-For"); return forwarded==null||forwarded.isBlank()?request.getRemoteAddr():forwarded.split(",")[0].trim();}
 private String json(Map<String,Object> details){if(details==null||details.isEmpty()) return null; try{return mapper.writeValueAsString(details);}catch(JsonProcessingException e){throw new IllegalArgumentException("Audit details must be JSON serializable.",e);}}
 private static <T>T required(T v){if(v==null)throw new IllegalArgumentException("Required audit field is missing.");return v;} private static String truncate(String v,int max){return v==null?null:v.substring(0,Math.min(v.length(),max));}
}
