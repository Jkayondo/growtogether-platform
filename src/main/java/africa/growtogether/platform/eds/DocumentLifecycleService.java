package africa.growtogether.platform.eds;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import java.time.*; import java.util.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class DocumentLifecycleService {
 private final DocumentRepository documents; private final DocumentVersionRepository versions; private final DocumentLifecycleEventRepository events; private final EnterpriseIdentityContext identity;
 public DocumentLifecycleService(DocumentRepository d,DocumentVersionRepository v,DocumentLifecycleEventRepository e,EnterpriseIdentityContext i){documents=d;versions=v;events=e;identity=i;}
 @Transactional public DocumentDtos.LifecycleView create(DocumentDtos.CreateDocument c){UUID t=identity.requireTenantId();if(versions.existsByTenantIdAndChecksum(t,c.checksum()))throw new IllegalStateException("Duplicate document content detected");Document d=new Document(t,c.documentNumber(),c.title(),c.classification());documents.save(d);d.activateFirstVersion();versions.save(new DocumentVersion(t,d.id(),1,c.storageKey(),c.checksum(),c.mimeType(),c.sizeBytes(),c.changeSummary()));record(d,"DOCUMENT_CREATED",c.changeSummary());return view(d);}
 @Transactional public DocumentDtos.LifecycleView addVersion(UUID id,DocumentDtos.AddVersion c){Document d=get(id);if(versions.existsByTenantIdAndChecksum(identity.requireTenantId(),c.checksum()))throw new IllegalStateException("Duplicate document content detected");int n=d.nextVersion();versions.save(new DocumentVersion(identity.requireTenantId(),d.id(),n,c.storageKey(),c.checksum(),c.mimeType(),c.sizeBytes(),c.changeSummary()));record(d,"VERSION_CREATED","version="+n);return view(d);}
 @Transactional public DocumentDtos.LifecycleView checkOut(UUID id){Document d=get(id);d.checkOut(identity.requireUserId(),Instant.now());record(d,"CHECKED_OUT",null);return view(d);}
 @Transactional public DocumentDtos.LifecycleView checkIn(UUID id){Document d=get(id);d.checkIn(identity.requireUserId());record(d,"CHECKED_IN",null);return view(d);}
 @Transactional public DocumentDtos.LifecycleView retention(UUID id,Instant until){Document d=get(id);d.applyRetention(until);record(d,"RETENTION_UPDATED",String.valueOf(until));return view(d);}
 @Transactional public DocumentDtos.LifecycleView legalHold(UUID id,boolean enabled){Document d=get(id);if(enabled)d.placeLegalHold();else d.releaseLegalHold();record(d,enabled?"LEGAL_HOLD_PLACED":"LEGAL_HOLD_RELEASED",null);return view(d);}
 @Transactional public DocumentDtos.LifecycleView archive(UUID id){Document d=get(id);d.archive(Instant.now());record(d,"ARCHIVED",null);return view(d);}
 @Transactional public DocumentDtos.LifecycleView restore(UUID id){Document d=get(id);d.restore();record(d,"RESTORED",null);return view(d);}
 @Transactional public DocumentDtos.LifecycleView delete(UUID id){Document d=get(id);d.softDelete(Instant.now());record(d,"SOFT_DELETED",null);return view(d);}
 @Transactional public DocumentDtos.LifecycleView dispose(UUID id){Document d=get(id);d.dispose(Instant.now());record(d,"DISPOSED",null);return view(d);}
 @Transactional(readOnly=true) public List<DocumentVersion> versions(UUID id){get(id);return versions.findByDocumentIdAndTenantIdOrderByVersionNumberDesc(id,identity.requireTenantId());}
 private Document get(UUID id){return documents.findByIdAndTenantId(id,identity.requireTenantId()).orElseThrow(()->new NoSuchElementException("Document not found"));}
 private void record(Document d,String type,String details){events.save(new DocumentLifecycleEvent(identity.requireTenantId(),d.id(),type,details,Instant.now()));}
 private static DocumentDtos.LifecycleView view(Document d){return new DocumentDtos.LifecycleView(d.id(),d.documentNumber(),d.title(),d.documentStatus(),d.currentVersion(),d.retentionUntil(),d.legalHold(),d.checkedOutBy());}
}
