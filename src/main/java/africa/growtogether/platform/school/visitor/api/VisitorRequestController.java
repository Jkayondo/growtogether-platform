package africa.growtogether.platform.school.visitor.api;


import africa.growtogether.platform.school.visitor.api.dto.CreateVisitorRequestRequest;
import africa.growtogether.platform.school.visitor.api.dto.VisitorRequestResponse;
import africa.growtogether.platform.school.visitor.domain.VisitorRequest;
import africa.growtogether.platform.school.visitor.domain.SchoolVisitor;
import africa.growtogether.platform.school.visitor.repository.SchoolVisitorRepository;
import africa.growtogether.platform.school.visitor.service.VisitorRequestService;


import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/school/visitor-requests")
public class VisitorRequestController {


    private final VisitorRequestService service;

    private final SchoolVisitorRepository visitorRepository;


    public VisitorRequestController(
            VisitorRequestService service,
            SchoolVisitorRepository visitorRepository
    ) {
        this.service = service;
        this.visitorRepository = visitorRepository;
    }


    @PostMapping
    public VisitorRequestResponse create(
            @RequestParam UUID tenantId,
            @RequestBody @Valid CreateVisitorRequestRequest request
    ) {

        SchoolVisitor visitor =
                visitorRepository.findByIdAndTenantId(
                        request.visitorId(),
                        tenantId
                )
                .orElseThrow(
                        () -> new RuntimeException("Visitor not found")
                );


        return map(
                service.createRequest(
                        tenantId,
                        visitor,
                        request.hostUserId(),
                        request.purpose(),
                        request.visitDate(),
                        request.expectedArrivalTime(),
                        request.expectedDepartureTime()
                )
        );
    }


    @GetMapping("/{id}")
    public VisitorRequestResponse get(
            @RequestParam UUID tenantId,
            @PathVariable UUID id
    ) {

        return map(
                service.getRequest(
                        tenantId,
                        id
                )
        );
    }


    @GetMapping
    public List<VisitorRequestResponse> list(
            @RequestParam UUID tenantId
    ) {

        return service.listRequests(tenantId)
                .stream()
                .map(this::map)
                .toList();
    }


    @PostMapping("/{id}/approve")
    public VisitorRequestResponse approve(
            @RequestParam UUID tenantId,
            @PathVariable UUID id,
            @RequestParam String approvedBy,
            @RequestParam(required = false) String comment
    ) {

        return map(
                service.approveRequest(
                        tenantId,
                        id,
                        approvedBy,
                        comment
                )
        );
    }


    @PostMapping("/{id}/reject")
    public VisitorRequestResponse reject(
            @RequestParam UUID tenantId,
            @PathVariable UUID id,
            @RequestParam String rejectedBy,
            @RequestParam(required = false) String comment
    ) {

        return map(
                service.rejectRequest(
                        tenantId,
                        id,
                        rejectedBy,
                        comment
                )
        );
    }


    @PostMapping("/{id}/complete")
    public VisitorRequestResponse complete(
            @RequestParam UUID tenantId,
            @PathVariable UUID id
    ) {

        return map(
                service.completeVisit(
                        tenantId,
                        id
                )
        );
    }


    private VisitorRequestResponse map(
            VisitorRequest request
    ) {

        return new VisitorRequestResponse(
                request.getId(),
                request.getVisitor().getId(),
                request.getHostUserId(),
                request.getPurpose(),
                request.getVisitDate(),
                request.getExpectedArrivalTime(),
                request.getExpectedDepartureTime(),
                request.getRequestStatus(),
                request.getApprovalComment(),
                request.getApprovedBy()
        );
    }
}
