package africa.growtogether.platform.school.academic.subject;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SubjectRepository
        extends JpaRepository<Subject, UUID> {

    Optional<Subject> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );



    Optional<Subject> findByTenantIdAndSubjectCode(
            UUID tenantId,
            String subjectCode
    );


    List<Subject> findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );

}
