package africa.growtogether.platform.eip;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext; import java.time.Instant; import java.util.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class EipAdministrationService {
 private final IntegrationMessageRepository messages; private final IntegrationRouteRepository routes; private final ExternalConnectorRepository connectors; private final ConnectorCertificationRepository certifications; private final EnterpriseIdentityContext identity;
 public EipAdministrationService(IntegrationMessageRepository m,IntegrationRouteRepository r,ExternalConnectorRepository c,ConnectorCertificationRepository cr,EnterpriseIdentityContext i){messages=m;routes=r;connectors=c;certifications=cr;identity=i;}
 @Transactional(readOnly=true) public Map<String,Object> summary(){UUID t=identity.tenantId(); long total=messages.findAll().stream().filter(x->x.getTenantId().equals(t)).count(); long dead=messages.findAll().stream().filter(x->x.getTenantId().equals(t)&&x.messageStatus()==IntegrationMessageStatus.DEAD_LETTER).count(); return Map.of("tenantId",t,"messages",total,"deadLetters",dead,"routes",routes.findByTenantIdOrderByPriorityAsc(t).size(),"connectors",connectors.findAll().stream().filter(x->x.getTenantId().equals(t)).count(),"certifications",certifications.findByTenantIdOrderByCreatedAtDesc(t).size());}
 @Transactional public ConnectorCertification createCertification(UUID connectorId,String environment){UUID t=identity.tenantId(); connectors.findByTenantIdAndId(t,connectorId).orElseThrow(); return certifications.save(new ConnectorCertification(t,connectorId,environment));}
 @Transactional public ConnectorCertification certify(UUID id,String evidence,Instant expiresAt,String notes){UUID t=identity.tenantId(); ConnectorCertification c=certifications.findByTenantIdAndId(t,id).orElseThrow(); c.certify(evidence,expiresAt,notes); return c;}
 @Transactional(readOnly=true) public List<ConnectorCertification> list(){return certifications.findByTenantIdOrderByCreatedAtDesc(identity.tenantId());}
}
