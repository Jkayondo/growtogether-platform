package africa.growtogether.platform.eds.admin;

import africa.growtogether.platform.eds.Document;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface DocumentAdministrationRepository extends JpaRepository<Document,UUID> {
 long countByTenantId(UUID tenantId);
 long countByTenantIdAndDocumentStatus(UUID tenantId, DocumentStatus status);
 long countByTenantIdAndClassification(UUID tenantId, DocumentClassification classification);
 long countByTenantIdAndLegalHoldTrue(UUID tenantId);
 long countByTenantIdAndRetentionUntilLessThanEqualAndDocumentStatusNot(UUID tenantId, Instant cutoff, DocumentStatus excluded);
 @Query("select d from Document d where d.tenantId=:tenantId and (:status is null or d.documentStatus=:status) and (:classification is null or d.classification=:classification) and (:legalHold is null or d.legalHold=:legalHold) and (:retentionBefore is null or d.retentionUntil<=:retentionBefore) order by d.updatedAt desc")
 Page<Document> governanceQueue(@Param("tenantId") UUID tenantId,@Param("status") DocumentStatus status,
  @Param("classification") DocumentClassification classification,@Param("legalHold") Boolean legalHold,
  @Param("retentionBefore") Instant retentionBefore, Pageable pageable);
}
