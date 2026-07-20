package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.api.*; import africa.growtogether.platform.eip.IntegrationDtos.*; import jakarta.validation.Valid; import java.util.UUID; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/integration/runtime")
public class IntegrationRuntimeController { private final IntegrationRuntimeService service; public IntegrationRuntimeController(IntegrationRuntimeService service){this.service=service;}
 @PostMapping("/messages") @PreAuthorize("hasAuthority('integration.event.publish')") public ApiResponse<MessageView> publish(@Valid @RequestBody PublishCommand c){return ApiResponses.success(service.publish(c));}
 @GetMapping("/messages/{id}") @PreAuthorize("hasAuthority('integration.event.read')") public ApiResponse<MessageView> get(@PathVariable UUID id){return ApiResponses.success(service.get(id));}
 @PostMapping("/messages/{id}/dispatch") @PreAuthorize("hasAuthority('integration.runtime.manage')") public ApiResponse<MessageView> dispatch(@PathVariable UUID id){return ApiResponses.success(service.dispatch(id));}
 @PostMapping("/messages/{id}/delivered") @PreAuthorize("hasAuthority('integration.runtime.manage')") public ApiResponse<MessageView> delivered(@PathVariable UUID id){return ApiResponses.success(service.delivered(id));}
 @PostMapping("/messages/{id}/failed") @PreAuthorize("hasAuthority('integration.runtime.manage')") public ApiResponse<MessageView> failed(@PathVariable UUID id,@Valid @RequestBody FailCommand c){return ApiResponses.success(service.failed(id,c));}
 @PostMapping("/messages/{id}/replay") @PreAuthorize("hasAuthority('integration.replay')") public ApiResponse<MessageView> replay(@PathVariable UUID id,@RequestParam String idempotencyKey){return ApiResponses.success(service.replay(id,idempotencyKey));}
 @PostMapping("/routes") @PreAuthorize("hasAuthority('integration.route.manage')") public ApiResponse<Void> route(@Valid @RequestBody RouteCommand c){service.addRoute(c);return ApiResponses.success(null);}
}
