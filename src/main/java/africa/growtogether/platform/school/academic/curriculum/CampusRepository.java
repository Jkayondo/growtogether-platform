package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CampusRepository
        extends JpaRepository<Campus, UUID> {

    Optional<Campus> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );



    List<Campus> findByTenantIdAndSchoolProfileId(
            UUID tenantId,
            UUID schoolProfileId
    );


    Optional<Campus> findByTenantIdAndCampusCode(
            UUID tenantId,
            String campusCode
    );


    List<Campus> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
