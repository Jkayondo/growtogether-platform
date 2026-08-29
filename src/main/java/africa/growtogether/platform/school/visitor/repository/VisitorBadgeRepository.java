package africa.growtogether.platform.school.visitor.repository;


import africa.growtogether.platform.school.visitor.domain.VisitorBadge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface VisitorBadgeRepository
        extends JpaRepository<VisitorBadge, UUID> {


    Optional<VisitorBadge> findByBadgeNumber(
            String badgeNumber
    );


    Optional<VisitorBadge> findByVisitorCheckInId(
            UUID visitorCheckInId
    );


    List<VisitorBadge> findAllByVisitorCheckInId(
            UUID visitorCheckInId
    );


    List<VisitorBadge> findAllByStatus(
            africa.growtogether.platform.common.persistence.EntityStatus status
    );

}
