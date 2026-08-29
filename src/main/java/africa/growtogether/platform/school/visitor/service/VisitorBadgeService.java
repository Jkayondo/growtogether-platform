package africa.growtogether.platform.school.visitor.service;


import africa.growtogether.platform.school.visitor.domain.VisitorBadge;
import africa.growtogether.platform.school.visitor.repository.VisitorBadgeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class VisitorBadgeService {


    private final VisitorBadgeRepository repository;


    public VisitorBadgeService(
            VisitorBadgeRepository repository
    ) {

        this.repository = repository;

    }


    public VisitorBadge issueBadge(
            UUID visitorCheckInId,
            String badgeNumber,
            String badgeType,
            String issuedBy
    ) {


        repository.findByBadgeNumber(badgeNumber)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Badge number already exists."
                    );
                });


        VisitorBadge badge =
                new VisitorBadge(
                        visitorCheckInId,
                        badgeNumber,
                        badgeType,
                        issuedBy
                );


        return repository.save(badge);

    }


    @Transactional(readOnly = true)
    public VisitorBadge getBadge(
            UUID id
    ) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Visitor badge not found."
                        )
                );

    }


    @Transactional(readOnly = true)
    public VisitorBadge findByBadgeNumber(
            String badgeNumber
    ) {

        return repository.findByBadgeNumber(badgeNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Visitor badge not found."
                        )
                );

    }


    @Transactional(readOnly = true)
    public List<VisitorBadge> findByCheckIn(
            UUID visitorCheckInId
    ) {

        return repository.findAllByVisitorCheckInId(
                visitorCheckInId
        );

    }


    public VisitorBadge returnBadge(
            UUID badgeId,
            String returnedBy
    ) {

        VisitorBadge badge =
                getBadge(badgeId);


        badge.returnBadge(
                returnedBy
        );


        return repository.save(badge);

    }


    public VisitorBadge disableBadge(
            UUID badgeId
    ) {

        VisitorBadge badge =
                getBadge(badgeId);


        badge.disable();


        return repository.save(badge);

    }

}
