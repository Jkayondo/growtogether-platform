package africa.growtogether.platform.eip;
import jakarta.validation.constraints.*; import java.util.UUID;
public final class EipGatewayDtos {private EipGatewayDtos(){}
 public record GatewayRouteCommand(@NotBlank String routeCode,@NotBlank String pathPattern,@NotBlank String httpMethod,@NotBlank String upstreamUri,String requiredAuthority,@Min(1) int rateLimitPerMinute,@Min(1) int timeoutMillis){}
 public record WebhookCommand(@NotBlank String subscriptionCode,@NotBlank String eventPattern,@NotBlank String callbackUrl,@NotBlank String signingSecret){}
 public record TransformCommand(@NotBlank String ruleCode,@NotBlank String sourceContentType,@NotBlank String targetContentType,@NotBlank String mappingExpression){}
 public record ConnectorCommand(@NotBlank String connectorCode,@NotBlank String connectorType,@NotBlank String baseUrl,@NotBlank String authType,String credential){}
 public record GatewayRouteView(UUID id,String routeCode,String pathPattern,String httpMethod,String upstreamUri,String requiredAuthority,int rateLimitPerMinute,int timeoutMillis,boolean active){static GatewayRouteView from(GatewayRoute r){return new GatewayRouteView(r.id(),r.routeCode(),r.pathPattern(),r.httpMethod(),r.upstreamUri(),r.requiredAuthority(),r.rateLimitPerMinute(),r.timeoutMillis(),r.active());}}
 public record WebhookView(UUID id,String subscriptionCode,String eventPattern,String callbackUrl,boolean active){static WebhookView from(WebhookSubscription s){return new WebhookView(s.id(),s.subscriptionCode(),s.eventPattern(),s.callbackUrl(),s.active());}}
 public record TransformationView(UUID id,String ruleCode,String sourceContentType,String targetContentType,String mappingExpression){static TransformationView from(TransformationRule r){return new TransformationView(r.id(),r.ruleCode(),r.sourceContentType(),r.targetContentType(),r.mappingExpression());}}
 public record ConnectorView(UUID id,String connectorCode,String connectorType,String baseUrl,String authType,boolean active){static ConnectorView from(ExternalConnector c){return new ConnectorView(c.id(),c.connectorCode(),c.connectorType(),c.baseUrl(),c.authType(),c.active());}}
}
