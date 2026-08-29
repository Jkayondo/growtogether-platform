package africa.growtogether.platform.school.visitor.api;


import africa.growtogether.platform.school.visitor.api.dto.*;
import africa.growtogether.platform.school.visitor.domain.VisitorBadge;
import africa.growtogether.platform.school.visitor.service.VisitorBadgeService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/visitor-badges")
public class VisitorBadgeController {


    private final VisitorBadgeService service;


    public VisitorBadgeController(
            VisitorBadgeService service
    ) {

        this.service = service;

    }


    @PostMapping
    public ResponseEntity<VisitorBadgeResponse> issue(
            @RequestBody @Valid IssueVisitorBadgeRequest request
    ) {

        VisitorBadge badge =
                service.issueBadge(
                        request.visitorCheckInId(),
                        request.badgeNumber(),
                        request.badgeType(),
                        request.issuedBy()
                );


        return ResponseEntity.ok(
                VisitorBadgeResponse.from(badge)
        );

    }


    @GetMapping("/{id}")
    public ResponseEntity<VisitorBadgeResponse> get(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                VisitorBadgeResponse.from(
                        service.getBadge(id)
                )
        );

    }


    @GetMapping("/number/{badgeNumber}")
    public ResponseEntity<VisitorBadgeResponse> findByNumber(
            @PathVariable String badgeNumber
    ) {

        return ResponseEntity.ok(
                VisitorBadgeResponse.from(
                        service.findByBadgeNumber(
                                badgeNumber
                        )
                )
        );

    }


    @GetMapping("/check-in/{visitorCheckInId}")
    public ResponseEntity<List<VisitorBadgeResponse>> findByCheckIn(
            @PathVariable UUID visitorCheckInId
    ) {

        return ResponseEntity.ok(

                service.findByCheckIn(visitorCheckInId)
                        .stream()
                        .map(VisitorBadgeResponse::from)
                        .toList()

        );

    }


    @PostMapping("/{id}/return")
    public ResponseEntity<VisitorBadgeResponse> returnBadge(
            @PathVariable UUID id,
            @RequestBody @Valid ReturnVisitorBadgeRequest request
    ) {

        return ResponseEntity.ok(

                VisitorBadgeResponse.from(
                        service.returnBadge(
                                id,
                                request.returnedBy()
                        )
                )

        );

    }


    @PostMapping("/{id}/disable")
    public ResponseEntity<VisitorBadgeResponse> disable(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(

                VisitorBadgeResponse.from(
                        service.disableBadge(id)
                )

        );

    }

}
