package africa.growtogether.platform.school.visitor.service;


import africa.growtogether.platform.school.visitor.domain.VisitorCheckIn;
import africa.growtogether.platform.school.visitor.domain.VisitorRequest;
import africa.growtogether.platform.school.visitor.repository.VisitorCheckInRepository;
import africa.growtogether.platform.school.visitor.repository.VisitorRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class VisitorCheckInService {


    private final VisitorCheckInRepository checkInRepository;

    private final VisitorRequestRepository requestRepository;


    public VisitorCheckInService(
            VisitorCheckInRepository checkInRepository,
            VisitorRequestRepository requestRepository
    ) {
        this.checkInRepository = checkInRepository;
        this.requestRepository = requestRepository;
    }


    public VisitorCheckIn checkIn(
            UUID tenantId,
            UUID visitorRequestId,
            String gateLocation,
            String badgeNumber,
            String checkedInBy
    ) {

        VisitorRequest request =
                requestRepository
                        .findByIdAndTenantId(
                                visitorRequestId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Visitor request not found"
                                )
                        );


        if (!"APPROVED".equals(request.getRequestStatus())) {

            throw new IllegalStateException(
                    "Visitor request must be approved before check-in"
            );
        }


        boolean alreadyCheckedIn =
                checkInRepository
                        .findFirstByTenantIdAndVisitorIdAndCheckInStatus(
                                tenantId,
                                request.getVisitor().getId(),
                                "CHECKED_IN"
                        )
                        .isPresent();


        if (alreadyCheckedIn) {

            throw new IllegalStateException(
                    "Visitor is already checked in"
            );
        }


        VisitorCheckIn checkIn =
                new VisitorCheckIn(
                        request,
                        request.getVisitor(),
                        gateLocation,
                        badgeNumber,
                        checkedInBy
                );


        return checkInRepository.save(checkIn);
    }


    public VisitorCheckIn checkOut(
            UUID tenantId,
            UUID checkInId,
            String checkedOutBy
    ) {

        VisitorCheckIn checkIn =
                checkInRepository
                        .findByIdAndTenantId(
                                checkInId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Visitor check-in not found"
                                )
                        );

        if (!"CHECKED_IN".equals(checkIn.getCheckInStatus())) {

    		throw new IllegalStateException(
            		"Visitor is not currently checked in"
    		);
	}


        checkIn.checkOut(checkedOutBy);


        return checkInRepository.save(checkIn);
    }


    @Transactional(readOnly = true)
    public List<VisitorCheckIn> activeVisitors(
            UUID tenantId
    ) {

        return checkInRepository
                .findAllByTenantIdAndCheckInStatus(
                        tenantId,
                        "CHECKED_IN"
                );
    }
}
