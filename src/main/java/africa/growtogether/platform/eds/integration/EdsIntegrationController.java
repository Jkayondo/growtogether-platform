package africa.growtogether.platform.eds.integration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/documents")
public class EdsIntegrationController {
 private final EdsIntegrationService service; private final EdsNotificationGateway notifications;
 public EdsIntegrationController(EdsIntegrationService s,EdsNotificationGateway n){service=s;notifications=n;}
 @PostMapping("/{id}/workflow-links") @PreAuthorize("hasAuthority('document.workflow.manage')")
 public EdsIntegrationService.LinkView attach(@PathVariable UUID id,@Valid @RequestBody WorkflowLinkRequest r){return service.attach(id,r.workflowInstanceId(),r.relationshipType());}
 @GetMapping("/workflow/{workflowInstanceId}") @PreAuthorize("hasAuthority('document.workflow.read')")
 public List<EdsIntegrationService.LinkView> links(@PathVariable UUID workflowInstanceId){return service.workflowLinks(workflowInstanceId);}
 @PostMapping("/{id}/ai-requests") @PreAuthorize("hasAuthority('document.ai.execute')")
 public EdsIntegrationService.AiRequestView ai(@PathVariable UUID id,@Valid @RequestBody AiRequest r){return service.requestAi(id,r.operation());}
 @PostMapping("/{id}/events") @PreAuthorize("hasAuthority('document.events.publish')")
 public Map<String,UUID> event(@PathVariable UUID id,@Valid @RequestBody EventRequest r){return Map.of("eventId",service.publish(id,r.eventType(),r.payload()));}
 @PostMapping("/{id}/notifications/shared") @PreAuthorize("hasAuthority('document.notify')")
 public Map<String,UUID> notifyShared(@PathVariable UUID id,@Valid @RequestBody NotificationRequest r){return Map.of("notificationId",notifications.documentShared(id,r.recipient(),r.title()));}
 public record WorkflowLinkRequest(@NotNull UUID workflowInstanceId,String relationshipType){}
 public record AiRequest(@NotBlank String operation){}
 public record EventRequest(@NotBlank String eventType,Map<String,Object> payload){}
 public record NotificationRequest(@NotBlank String recipient,@NotBlank String title){}
}
