package africa.growtogether.platform.eiam.audit;
import java.util.Map;
public record RecordAuditEventCommand(String eventType, AuditEventCategory category, AuditOutcome outcome, SecuritySeverity severity, String resourceType, String resourceId, String message, Map<String,Object> details) {}
