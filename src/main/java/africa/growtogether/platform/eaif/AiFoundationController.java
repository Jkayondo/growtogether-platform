package africa.growtogether.platform.eaif;
import jakarta.validation.Valid; import java.util.UUID; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/ai")
public class AiFoundationController {
 private final AiFoundationService service; public AiFoundationController(AiFoundationService s){service=s;}
 private UUID tenant(String value){return UUID.fromString(value);}
 @PostMapping("/providers") @PreAuthorize("hasAuthority('ai.provider.manage')") AiProvider provider(@RequestHeader("X-Tenant-ID") String t,@Valid @RequestBody CreateProviderRequest q){return service.createProvider(tenant(t),q);}
 @PostMapping("/models") @PreAuthorize("hasAuthority('ai.model.manage')") AiModel model(@RequestHeader("X-Tenant-ID") String t,@Valid @RequestBody CreateModelRequest q){return service.createModel(tenant(t),q);}
 @PostMapping("/prompts") @PreAuthorize("hasAuthority('ai.prompt.manage')") PromptTemplate prompt(@RequestHeader("X-Tenant-ID") String t,@Valid @RequestBody CreatePromptRequest q){return service.createPrompt(tenant(t),q);}
 @PostMapping("/requests") @PreAuthorize("hasAuthority('ai.request.create')") AiRequestView request(@RequestHeader("X-Tenant-ID") String t,@Valid @RequestBody CreateAiRequest q){return AiRequestView.of(service.submit(tenant(t),q));}
 @GetMapping("/requests/{id}") @PreAuthorize("hasAuthority('ai.request.read')") AiRequestView get(@RequestHeader("X-Tenant-ID") String t,@PathVariable UUID id){return AiRequestView.of(service.get(tenant(t),id));}
 @PostMapping("/requests/{id}/processing") @PreAuthorize("hasAuthority('ai.runtime.execute')") AiRequestView begin(@RequestHeader("X-Tenant-ID") String t,@PathVariable UUID id){return AiRequestView.of(service.begin(tenant(t),id));}
 @PostMapping("/requests/{id}/succeeded") @PreAuthorize("hasAuthority('ai.runtime.execute')") AiRequestView succeed(@RequestHeader("X-Tenant-ID") String t,@PathVariable UUID id,@Valid @RequestBody CompleteAiRequest q){return AiRequestView.of(service.succeed(tenant(t),id,q.outputReference()));}
 @PostMapping("/requests/{id}/failed") @PreAuthorize("hasAuthority('ai.runtime.execute')") AiRequestView fail(@RequestHeader("X-Tenant-ID") String t,@PathVariable UUID id,@Valid @RequestBody FailAiRequest q){return AiRequestView.of(service.fail(tenant(t),id,q.reason()));}
}
