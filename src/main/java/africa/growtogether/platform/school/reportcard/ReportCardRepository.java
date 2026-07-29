package africa.growtogether.platform.school.reportcard;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ReportCardRepository
        extends JpaRepository<ReportCard, UUID> {


    List<ReportCard> findByLearnerIdOrderByCreatedAtDesc(
            UUID learnerId
    );


    List<ReportCard> findByAcademicPeriodId(
            UUID academicPeriodId
    );


    List<ReportCard> findByTenantId(
            UUID tenantId
    );
}
