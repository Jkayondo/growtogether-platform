package africa.growtogether.platform.school.academic.term;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicTermRepository
        extends JpaRepository<AcademicTerm, UUID> {


    List<AcademicTerm> findByAcademicYearId(
            UUID academicYearId
    );


    List<AcademicTerm> findByAcademicYearIdAndStatus(
            UUID academicYearId,
            String status
    );


    List<AcademicTerm> findByTenantId(
            UUID tenantId
    );

}
