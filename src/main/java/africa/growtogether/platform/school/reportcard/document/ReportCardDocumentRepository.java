package africa.growtogether.platform.school.reportcard.document;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ReportCardDocumentRepository
        extends JpaRepository<ReportCardDocument, UUID> {


    List<ReportCardDocument> findByReportCardId(
            UUID reportCardId
    );


    List<ReportCardDocument> findByTenantId(
            UUID tenantId
    );

}
