package africa.growtogether.platform.eiam.audit;
import java.time.Instant; import java.util.UUID;
public record AuditEventView(UUID id,UUID tenantId,UUID actorUserId,String actorUsername,String eventType,AuditEventCategory category,AuditOutcome outcome,SecuritySeverity severity,String resourceType,String resourceId,String sourceIp,String correlationId,UUID sessionId,String message,String detailsJson,Instant occurredAt) {
 public static AuditEventView from(AuditEvent e){return new AuditEventView(e.getId(),e.getTenantId(),e.getActorUserId(),e.getActorUsername(),e.getEventType(),e.getCategory(),e.getOutcome(),e.getSeverity(),e.getResourceType(),e.getResourceId(),e.getSourceIp(),e.getCorrelationId(),e.getSessionId(),e.getMessage(),e.getDetailsJson(),e.getOccurredAt());}
}
