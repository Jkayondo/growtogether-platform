package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eip.IntegrationDtos.*;
import java.time.*; import java.util.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class IntegrationRuntimeService {
 private final IntegrationMessageRepository messages; private final IntegrationRouteRepository routes; private final EnterpriseIdentityContext identity;
 public IntegrationRuntimeService(IntegrationMessageRepository messages,IntegrationRouteRepository routes,EnterpriseIdentityContext identity){this.messages=messages;this.routes=routes;this.identity=identity;}
 @Transactional public MessageView publish(PublishCommand c){UUID tenant=identity.tenantId();return messages.findByTenantIdAndIdempotencyKey(tenant,c.idempotencyKey()).map(MessageView::from).orElseGet(()->{IntegrationMessage m=new IntegrationMessage(tenant,c.eventType(),c.eventVersion(),c.sourceService(),c.destination(),c.protocol(),c.payload(),c.headersJson(),c.correlationId(),c.idempotencyKey(),c.maxAttempts());m.route();return MessageView.from(messages.save(m));});}
 @Transactional(readOnly=true) public MessageView get(UUID id){return MessageView.from(require(id));}
 @Transactional public MessageView dispatch(UUID id){IntegrationMessage m=require(id);m.dispatch();return MessageView.from(m);}
 @Transactional public MessageView delivered(UUID id){IntegrationMessage m=require(id);m.delivered();return MessageView.from(m);}
 @Transactional public MessageView failed(UUID id,FailCommand c){IntegrationMessage m=require(id);m.fail(c.error(),Instant.now().plusSeconds(c.retryAfterSeconds()));return MessageView.from(m);}
 @Transactional public MessageView replay(UUID id,String newKey){IntegrationMessage copy=require(id).replay(newKey);copy.route();return MessageView.from(messages.save(copy));}
 @Transactional public void addRoute(RouteCommand c){routes.save(new IntegrationRoute(identity.tenantId(),c.routeCode(),c.eventPattern(),c.destination(),c.protocol(),c.priority()));}
 private IntegrationMessage require(UUID id){return messages.findByTenantIdAndId(identity.tenantId(),id).orElseThrow(()->new NoSuchElementException("Integration message not found"));}
}
