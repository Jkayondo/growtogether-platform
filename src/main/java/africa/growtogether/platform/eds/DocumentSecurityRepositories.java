package africa.growtogether.platform.eds;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
interface DocumentShareRepository extends JpaRepository<DocumentShare,UUID>{Optional<DocumentShare> findByIdAndTenantId(UUID id,UUID tenantId);Optional<DocumentShare> findByTokenHash(String tokenHash);List<DocumentShare> findByDocumentIdAndTenantIdOrderByExpiresAtDesc(UUID documentId,UUID tenantId);}
interface DocumentCollaborationEventRepository extends JpaRepository<DocumentCollaborationEvent,UUID>{}
interface DocumentSearchRepository extends JpaRepository<Document,UUID>{
 @Query("select d from Document d where d.tenantId=:tenantId and d.documentStatus<>africa.growtogether.platform.eds.DocumentStatus.DISPOSED and (lower(d.title) like lower(concat('%',:q,'%')) or lower(d.documentNumber) like lower(concat('%',:q,'%')) or lower(coalesce(d.description,'')) like lower(concat('%',:q,'%'))) order by d.updatedAt desc")
 List<Document> search(@Param("tenantId") UUID tenantId,@Param("q") String query);
}
