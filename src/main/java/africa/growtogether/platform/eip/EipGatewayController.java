package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.api.*; import static africa.growtogether.platform.eip.EipGatewayDtos.*; import jakarta.validation.Valid; import java.util.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/integration") public class EipGatewayController {private final EipGatewayService service;public EipGatewayController(EipGatewayService service){this.service=service;}
 @PostMapping("/gateway/routes") @PreAuthorize("hasAuthority('integration.gateway.manage')") public ApiResponse<GatewayRouteView> route(@Valid @RequestBody GatewayRouteCommand c){return ApiResponses.success(service.createRoute(c));}
 @GetMapping("/gateway/routes") @PreAuthorize("hasAuthority('integration.gateway.read')") public ApiResponse<List<GatewayRouteView>> routes(){return ApiResponses.success(service.listRoutes());}
 @PostMapping("/webhooks") @PreAuthorize("hasAuthority('integration.webhook.manage')") public ApiResponse<WebhookView> webhook(@Valid @RequestBody WebhookCommand c){return ApiResponses.success(service.createWebhook(c));}
 @GetMapping("/webhooks") @PreAuthorize("hasAuthority('integration.webhook.read')") public ApiResponse<List<WebhookView>> webhooks(){return ApiResponses.success(service.listWebhooks());}
 @PostMapping("/transformations") @PreAuthorize("hasAuthority('integration.transformation.manage')") public ApiResponse<TransformationView> transform(@Valid @RequestBody TransformCommand c){return ApiResponses.success(service.createTransform(c));}
 @GetMapping("/transformations") @PreAuthorize("hasAuthority('integration.transformation.read')") public ApiResponse<List<TransformationView>> transforms(){return ApiResponses.success(service.listTransforms());}
 @PostMapping("/connectors") @PreAuthorize("hasAuthority('integration.connector.manage')") public ApiResponse<ConnectorView> connector(@Valid @RequestBody ConnectorCommand c){return ApiResponses.success(service.createConnector(c));}
 @GetMapping("/connectors") @PreAuthorize("hasAuthority('integration.connector.read')") public ApiResponse<List<ConnectorView>> connectors(){return ApiResponses.success(service.listConnectors());}
}
