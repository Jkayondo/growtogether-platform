package africa.growtogether.platform.ens;
import africa.growtogether.platform.common.api.*; import africa.growtogether.platform.ens.NotificationDtos.*; import jakarta.validation.Valid; import java.util.UUID; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/notifications")
public class NotificationController { private final NotificationService service; public NotificationController(NotificationService service){this.service=service;}
 @PostMapping @PreAuthorize("hasAuthority('notification.send')") public ApiResponse<View> send(@Valid @RequestBody SendCommand command){return ApiResponses.success(service.send(command));}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('notification.read')") public ApiResponse<View> get(@PathVariable UUID id){return ApiResponses.success(service.get(id));}
 @PostMapping("/{id}/processing") @PreAuthorize("hasAuthority('notification.queue.manage')") public ApiResponse<View> processing(@PathVariable UUID id){return ApiResponses.success(service.markProcessing(id));}
 @PostMapping("/{id}/sent") @PreAuthorize("hasAuthority('notification.queue.manage')") public ApiResponse<View> sent(@PathVariable UUID id,@RequestParam String providerReference){return ApiResponses.success(service.markSent(id,providerReference));}
 @PostMapping("/{id}/delivered") @PreAuthorize("hasAuthority('notification.queue.manage')") public ApiResponse<View> delivered(@PathVariable UUID id){return ApiResponses.success(service.markDelivered(id));}
 @PostMapping("/{id}/failed") @PreAuthorize("hasAuthority('notification.queue.manage')") public ApiResponse<View> failed(@PathVariable UUID id,@RequestParam String error){return ApiResponses.success(service.markFailed(id,error));}
}
