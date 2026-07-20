package africa.growtogether.platform.eds.admin;

import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DocumentAdministrationDtos {
 private DocumentAdministrationDtos() {}
 public record GovernanceSummary(long totalDocuments, Map<DocumentStatus,Long> byStatus,
   Map<DocumentClassification,Long> byClassification,long legalHoldCount,long retentionDueCount,
   Instant generatedAt) {}
 public record GovernanceDocument(UUID id,String documentNumber,String title,DocumentStatus status,
   DocumentClassification classification,Instant retentionUntil,boolean legalHold,int currentVersion) {}
 public record GovernancePage(List<GovernanceDocument> items,int page,int size,long totalElements,int totalPages) {}
 public record StorageHealth(String provider,String state,long objectCount,long bytesTracked,Instant checkedAt) {}
}
