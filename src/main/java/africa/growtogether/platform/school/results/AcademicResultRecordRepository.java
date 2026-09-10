package africa.growtogether.platform.school.results;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicResultRecordRepository
        extends JpaRepository<AcademicResultRecord, UUID> {



    List<AcademicResultRecord>
    findByTenantIdAndStudentIdOrderByCreatedAtDesc(

            UUID tenantId,

            UUID studentId

    );



    List<AcademicResultRecord>
    findByTenantIdAndStudentIdAndAcademicYearIdOrderByCreatedAtDesc(

            UUID tenantId,

            UUID studentId,

            UUID academicYearId

    );



    List<AcademicResultRecord>
    findByTenantIdAndGradingSchemeIdOrderByCreatedAtDesc(

            UUID tenantId,

            UUID gradingSchemeId

    );


}
