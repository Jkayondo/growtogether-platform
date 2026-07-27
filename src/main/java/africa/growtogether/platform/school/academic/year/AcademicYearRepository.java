package africa.growtogether.platform.school.academic.year;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicYearRepository
        extends JpaRepository<AcademicYear, UUID> {


    List<AcademicYear> findByTenantId(
            UUID tenantId
    );


    List<AcademicYear> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
