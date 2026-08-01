package africa.growtogether.platform.school.visitor.repository;

import africa.growtogether.platform.school.visitor.domain.SchoolVisitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SchoolVisitorRepository extends JpaRepository<SchoolVisitor, UUID> {


    Optional<SchoolVisitor> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );


    List<SchoolVisitor> findAllByTenantId(
            UUID tenantId
    );


    Optional<SchoolVisitor> findByTenantIdAndIdentificationReference(
            UUID tenantId,
            String identificationReference
    );
}
