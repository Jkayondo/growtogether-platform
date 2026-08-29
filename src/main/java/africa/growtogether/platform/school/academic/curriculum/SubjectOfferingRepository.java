package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SubjectOfferingRepository
        extends JpaRepository<SubjectOffering, UUID> {

    Optional<SubjectOffering> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );



    List<SubjectOffering> findByTenantIdAndClassOfferingId(
            UUID tenantId,
            UUID classOfferingId
    );


    List<SubjectOffering> findByTenantIdAndSubjectId(
            UUID tenantId,
            UUID subjectId
    );


    Optional<SubjectOffering> findByTenantIdAndSubjectOfferingCode(
            UUID tenantId,
            String subjectOfferingCode
    );


    List<SubjectOffering> findByTenantIdAndOfferingStatus(
            UUID tenantId,
            String offeringStatus
    );

}