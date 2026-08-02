package africa.growtogether.platform.school.visitor.service;


import africa.growtogether.platform.school.visitor.domain.SchoolVisitor;
import africa.growtogether.platform.school.visitor.domain.VisitorRequest;
import africa.growtogether.platform.school.visitor.repository.VisitorRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class VisitorRequestService {


    private final VisitorRequestRepository repository;


    public VisitorRequestService(
            VisitorRequestRepository repository
    ) {
        this.repository = repository;
    }


    public VisitorRequest createRequest(
            UUID tenantId,
            SchoolVisitor visitor,
            UUID hostUserId,
            String purpose,
            LocalDate visitDate,
            LocalTime arrivalTime,
            LocalTime departureTime
    ) {

        if (visitDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Visit date cannot be in the past"
            );
        }


        if (arrivalTime != null
                && departureTime != null
                && departureTime.isBefore(arrivalTime)) {

            throw new IllegalArgumentException(
                    "Departure time cannot be before arrival time"
            );
        }


        VisitorRequest request =
                new VisitorRequest(
                        visitor,
                        hostUserId,
                        purpose,
                        visitDate,
                        arrivalTime,
                        departureTime
                );


        return repository.save(request);
    }


    @Transactional(readOnly = true)
    public VisitorRequest getRequest(
            UUID tenantId,
            UUID requestId
    ) {

        return repository
                .findByIdAndTenantId(requestId, tenantId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Visitor request not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<VisitorRequest> listRequests(
            UUID tenantId
    ) {

        return repository.findAllByTenantId(tenantId);
    }


    public VisitorRequest approveRequest(
            UUID tenantId,
            UUID requestId,
            String approvedBy,
            String comment
    ) {

        VisitorRequest request =
                getRequest(tenantId, requestId);

        request.approve(
                approvedBy,
                comment
        );

        return repository.save(request);
    }


    public VisitorRequest rejectRequest(
            UUID tenantId,
            UUID requestId,
            String rejectedBy,
            String comment
    ) {

        VisitorRequest request =
                getRequest(tenantId, requestId);

        request.reject(
                rejectedBy,
                comment
        );

        return repository.save(request);
    }


    public VisitorRequest completeVisit(
            UUID tenantId,
            UUID requestId
    ) {

        VisitorRequest request =
                getRequest(tenantId, requestId);

        request.complete();

        return repository.save(request);
    }
}
