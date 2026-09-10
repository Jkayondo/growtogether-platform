package africa.growtogether.platform.school.results.audit;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicResultAuditEventRepository
        extends JpaRepository<AcademicResultAuditEvent, UUID> {


    List<AcademicResultAuditEvent>
    findByTenantIdAndStudentSubjectResultIdOrderByPerformedAtAsc(
            UUID tenantId,
            UUID studentSubjectResultId
    );


}
