package africa.growtogether.platform.eds.admin;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.Document;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentStatus;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentAdministrationService {
 private final DocumentAdministrationRepository repository;
 private final EnterpriseIdentityContext identity;
 private final Clock clock;
 private final MeterRegistry meters;
 public DocumentAdministrationService(DocumentAdministrationRepository repository,EnterpriseIdentityContext identity,MeterRegistry meters){
  this.repository=repository;this.identity=identity;this.meters=meters;this.clock=Clock.systemUTC();
 }
 @Transactional(readOnly=true)
 public DocumentAdministrationDtos.GovernanceSummary summary(){
  var tenant=identity.requireTenantId();
  Map<DocumentStatus,Long> statuses=new EnumMap<>(DocumentStatus.class);
  for(var s:DocumentStatus.values()) statuses.put(s,repository.countByTenantIdAndDocumentStatus(tenant,s));
  Map<DocumentClassification,Long> classes=new EnumMap<>(DocumentClassification.class);
  for(var c:DocumentClassification.values()) classes.put(c,repository.countByTenantIdAndClassification(tenant,c));
  var total=repository.countByTenantId(tenant);
  var hold=repository.countByTenantIdAndLegalHoldTrue(tenant);
  var due=repository.countByTenantIdAndRetentionUntilLessThanEqualAndDocumentStatusNot(tenant,Instant.now(clock),DocumentStatus.DISPOSED);
  meters.gauge("eds.documents.total",total);
  meters.gauge("eds.documents.legal_hold",hold);
  meters.gauge("eds.documents.retention_due",due);
  return new DocumentAdministrationDtos.GovernanceSummary(total,statuses,classes,hold,due,Instant.now(clock));
 }
 @Transactional(readOnly=true)
 public DocumentAdministrationDtos.GovernancePage queue(DocumentStatus status,DocumentClassification classification,Boolean legalHold,Instant retentionBefore,int page,int size){
  int safeSize=Math.min(Math.max(size,1),100); int safePage=Math.max(page,0);
  var result=repository.governanceQueue(identity.requireTenantId(),status,classification,legalHold,retentionBefore,PageRequest.of(safePage,safeSize));
  var items=result.getContent().stream().map(this::view).toList();
  return new DocumentAdministrationDtos.GovernancePage(items,safePage,safeSize,result.getTotalElements(),result.getTotalPages());
 }
 private DocumentAdministrationDtos.GovernanceDocument view(Document d){return new DocumentAdministrationDtos.GovernanceDocument(d.id(),d.documentNumber(),d.title(),d.documentStatus(),d.classification(),d.retentionUntil(),d.legalHold(),d.currentVersion());}
 public DocumentAdministrationDtos.StorageHealth storageHealth(){return new DocumentAdministrationDtos.StorageHealth("REFERENCE_ONLY","NOT_CONFIGURED",0,0,Instant.now(clock));}
}
