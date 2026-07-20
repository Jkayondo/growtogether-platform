package africa.growtogether.platform.ens;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.ens.NotificationDtos.*;
import africa.growtogether.platform.ens.integration.EnsAuditRecorder;
import africa.growtogether.platform.ens.integration.EnsConfigurationGateway;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.Instant; import java.util.UUID;
@Service
public class NotificationService {
 private final NotificationRequestRepository repo; private final EnterpriseIdentityContext identity; private final EnsConfigurationGateway config; private final EnsAuditRecorder audit;
 public NotificationService(NotificationRequestRepository repo,EnterpriseIdentityContext identity,EnsConfigurationGateway config,EnsAuditRecorder audit){this.repo=repo;this.identity=identity;this.config=config;this.audit=audit;}
 @Transactional public View send(SendCommand c){ UUID tenant=identity.tenantId(); String correlation=RequestContextHolder.current().map(x->x.correlationId()).orElse(null); NotificationRequest n=new NotificationRequest(tenant,c.definitionCode(),c.recipient(),c.channel(),c.priority(),c.subject(),c.body(),correlation,c.sourceService()==null?"UNKNOWN":c.sourceService(),c.sourceReference()); n.queue(); repo.save(n); audit.requested(n); return view(n); }
 @Transactional(readOnly=true) public View get(UUID id){ return view(repo.findByIdAndTenantId(id,identity.tenantId()).orElseThrow(()->new IllegalArgumentException("Notification not found"))); }
 @Transactional public View markProcessing(UUID id){var n=entity(id);n.processing();audit.processing(n);return view(n);}
 @Transactional public View markSent(UUID id,String providerReference){var n=entity(id);n.sent(providerReference);audit.sent(n);return view(n);}
 @Transactional public View markDelivered(UUID id){var n=entity(id);n.delivered();audit.delivered(n);return view(n);}
 @Transactional public View markFailed(UUID id,String error){var n=entity(id);int max=config.maxAttempts();long backoff=config.retryBackoffSeconds(n.attemptCount()+1);n.fail(error,Instant.now().plusSeconds(backoff),max);audit.failed(n);return view(n);}
 private NotificationRequest entity(UUID id){return repo.findByIdAndTenantId(id,identity.tenantId()).orElseThrow(()->new IllegalArgumentException("Notification not found"));}
 private static View view(NotificationRequest n){return new View(n.id(),n.getTenantId(),n.definitionCode(),n.recipient(),n.channel(),n.priority(),n.notificationStatus(),n.subject(),n.sourceService(),n.sourceReference(),n.attemptCount(),n.nextAttemptAt(),n.providerReference(),n.lastError());}
}
