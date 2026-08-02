package africa.growtogether.platform.school.visitor.repository;


import africa.growtogether.platform.school.visitor.domain.VisitorCheckIn;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface VisitorCheckInRepository
        extends JpaRepository<VisitorCheckIn, UUID> {


    Optional<VisitorCheckIn> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );


    List<VisitorCheckIn> findAllByTenantId(
            UUID tenantId
    );


    List<VisitorCheckIn> findAllByTenantIdAndCheckInStatus(
            UUID tenantId,
            String checkInStatus
    );


    Optional<VisitorCheckIn> findFirstByTenantIdAndVisitorIdAndCheckInStatus(
            UUID tenantId,
            UUID visitorId,
            String checkInStatus
    );


    Optional<VisitorCheckIn> findFirstByTenantIdAndVisitorRequestId(
            UUID tenantId,
            UUID visitorRequestId
    );
}
