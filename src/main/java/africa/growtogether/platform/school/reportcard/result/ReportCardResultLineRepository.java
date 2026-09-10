package africa.growtogether.platform.school.reportcard.result;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface ReportCardResultLineRepository
        extends JpaRepository<ReportCardResultLine, UUID> {


    List<ReportCardResultLine>
    findByTenantIdAndReportCardIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID reportCardId
    );


    boolean existsByTenantIdAndReportCardIdAndStudentSubjectResultId(
            UUID tenantId,
            UUID reportCardId,
            UUID studentSubjectResultId
    );

}
