package africa.growtogether.platform.eds;
import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="eds_documents", uniqueConstraints=@UniqueConstraint(name="uk_eds_document_tenant_number",columnNames={"tenant_id","document_number"}))
public class Document extends AuditedTenantEntity {
 @Column(name="document_number",nullable=false,length=80) private String documentNumber;
 @Column(nullable=false,length=240) private String title;
 @Column(columnDefinition="text") private String description;
 @Enumerated(EnumType.STRING) @Column(name="document_status",nullable=false,length=24) private DocumentStatus documentStatus=DocumentStatus.DRAFT;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private DocumentClassification classification=DocumentClassification.INTERNAL;
 @Column(name="current_version",nullable=false) private int currentVersion;
 @Column(name="retention_until") private Instant retentionUntil;
 @Column(name="legal_hold",nullable=false) private boolean legalHold;
 @Column(name="checked_out_by") private UUID checkedOutBy;
 @Column(name="checked_out_at") private Instant checkedOutAt;
 @Column(name="archived_at") private Instant archivedAt;
 @Column(name="deleted_at") private Instant deletedAt;
 protected Document(){}
 public Document(UUID tenantId,String documentNumber,String title,DocumentClassification classification){setTenantId(tenantId);this.documentNumber=req(documentNumber).toUpperCase();this.title=req(title);this.classification=classification==null?DocumentClassification.INTERNAL:classification;}
 private static String req(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("value is required");return v.trim();}
 public void activateFirstVersion(){if(currentVersion!=0)throw new IllegalStateException("Document already versioned");currentVersion=1;documentStatus=DocumentStatus.ACTIVE;}
 public int nextVersion(){if(documentStatus==DocumentStatus.DISPOSED||documentStatus==DocumentStatus.DELETED)throw new IllegalStateException("Document cannot be versioned");return ++currentVersion;}
 public void checkOut(UUID userId,Instant now){if(documentStatus!=DocumentStatus.ACTIVE)throw new IllegalStateException("Only active documents may be checked out");checkedOutBy=userId;checkedOutAt=now;documentStatus=DocumentStatus.CHECKED_OUT;}
 public void checkIn(UUID userId){if(documentStatus!=DocumentStatus.CHECKED_OUT||!userId.equals(checkedOutBy))throw new IllegalStateException("Checkout ownership mismatch");checkedOutBy=null;checkedOutAt=null;documentStatus=DocumentStatus.ACTIVE;}
 public void archive(Instant now){if(legalHold)throw new IllegalStateException("Document is under legal hold");if(documentStatus!=DocumentStatus.ACTIVE)throw new IllegalStateException("Only active documents may be archived");documentStatus=DocumentStatus.ARCHIVED;archivedAt=now;}
 public void restore(){if(documentStatus!=DocumentStatus.ARCHIVED&&documentStatus!=DocumentStatus.DELETED)throw new IllegalStateException("Document is not restorable");documentStatus=DocumentStatus.ACTIVE;archivedAt=null;deletedAt=null;}
 public void softDelete(Instant now){if(legalHold)throw new IllegalStateException("Document is under legal hold");if(documentStatus==DocumentStatus.DISPOSED)throw new IllegalStateException("Document already disposed");documentStatus=DocumentStatus.DELETED;deletedAt=now;}
 public void dispose(Instant now){if(legalHold)throw new IllegalStateException("Document is under legal hold");if(retentionUntil!=null&&retentionUntil.isAfter(now))throw new IllegalStateException("Retention period has not expired");if(documentStatus!=DocumentStatus.DELETED&&documentStatus!=DocumentStatus.ARCHIVED)throw new IllegalStateException("Document must be archived or deleted before disposal");documentStatus=DocumentStatus.DISPOSED;}
 public void applyRetention(Instant until){retentionUntil=until;}
 public void placeLegalHold(){legalHold=true;} public void releaseLegalHold(){legalHold=false;}
 public void changeClassification(DocumentClassification value){if(value==null)throw new IllegalArgumentException("classification is required");classification=value;}
 public UUID id(){return getId();} public String documentNumber(){return documentNumber;} public String title(){return title;} public DocumentStatus documentStatus(){return documentStatus;} public DocumentClassification classification(){return classification;} public int currentVersion(){return currentVersion;} public Instant retentionUntil(){return retentionUntil;} public boolean legalHold(){return legalHold;} public UUID checkedOutBy(){return checkedOutBy;}
}
