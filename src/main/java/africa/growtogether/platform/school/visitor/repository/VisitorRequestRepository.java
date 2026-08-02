package africa.growtogether.platform.school.visitor.repository;


import africa.growtogether.platform.school.visitor.domain.VisitorRequest;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface VisitorRequestRepository
        extends JpaRepository<VisitorRequest, UUID> {


    Optional<VisitorRequest> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );


    List<VisitorRequest> findAllByTenantId(
            UUID tenantId
    );


    List<VisitorRequest> findByVisitorIdAndTenantId(
            UUID visitorId,
            UUID tenantId
    );


    List<VisitorRequest> findByRequestStatusAndTenantId(
            String requestStatus,
            UUID tenantId
    );


    List<VisitorRequest> findByStatusAndTenantId(
            EntityStatus status,
            UUID tenantId
    );

}
