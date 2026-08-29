package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ClassOfferingRepository
        extends JpaRepository<ClassOffering, UUID> {

    Optional<ClassOffering> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );



    Optional<ClassOffering> findByTenantIdAndOfferingCode(
            UUID tenantId,
            String offeringCode
    );


    List<ClassOffering> findByTenantIdAndAcademicYearId(
            UUID tenantId,
            UUID academicYearId
    );


    List<ClassOffering> findByTenantIdAndCampusId(
            UUID tenantId,
            UUID campusId
    );


    List<ClassOffering> findByTenantIdAndClassGradeId(
            UUID tenantId,
            UUID classGradeId
    );


    List<ClassOffering> findByTenantIdAndOfferingStatus(
            UUID tenantId,
            String offeringStatus
    );

}
