package africa.growtogether.platform.eip;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface GatewayRouteRepository extends JpaRepository<GatewayRoute,UUID>{List<GatewayRoute> findByTenantIdOrderByRouteCode(UUID tenantId);}
interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription,UUID>{List<WebhookSubscription> findByTenantIdOrderBySubscriptionCode(UUID tenantId);Optional<WebhookSubscription> findByTenantIdAndId(UUID tenantId,UUID id);}
interface TransformationRuleRepository extends JpaRepository<TransformationRule,UUID>{List<TransformationRule> findByTenantIdOrderByRuleCode(UUID tenantId);Optional<TransformationRule> findByTenantIdAndId(UUID tenantId,UUID id);}
interface ExternalConnectorRepository extends JpaRepository<ExternalConnector,UUID>{List<ExternalConnector> findByTenantIdOrderByConnectorCode(UUID tenantId);Optional<ExternalConnector> findByTenantIdAndId(UUID tenantId,UUID id);}
