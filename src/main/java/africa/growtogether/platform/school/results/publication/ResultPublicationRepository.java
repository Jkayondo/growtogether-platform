package africa.growtogether.platform.school.results.publication;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface ResultPublicationRepository
        extends JpaRepository<ResultPublication, UUID> {


    Optional<ResultPublication>
    findByTenantIdAndAcademicYearIdAndAcademicTermIdAndClassGradeIdAndPublicationType(
            UUID tenantId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            String publicationType
    );


    boolean existsByTenantIdAndAcademicYearIdAndAcademicTermIdAndClassGradeIdAndPublicationTypeAndPublicationStatus(
            UUID tenantId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            String publicationType,
            String publicationStatus
    );

}
