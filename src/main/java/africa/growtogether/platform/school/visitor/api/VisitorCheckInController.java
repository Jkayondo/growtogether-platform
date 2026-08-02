package africa.growtogether.platform.school.visitor.api;


import jakarta.validation.Valid;
import africa.growtogether.platform.school.visitor.api.dto.CheckInVisitorRequest;
import africa.growtogether.platform.school.visitor.api.dto.VisitorCheckInResponse;
import africa.growtogether.platform.school.visitor.domain.VisitorCheckIn;
import africa.growtogether.platform.school.visitor.service.VisitorCheckInService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/school/visitor-check-ins")
public class VisitorCheckInController {


    private final VisitorCheckInService service;


    public VisitorCheckInController(
            VisitorCheckInService service
    ) {
        this.service = service;
    }


    @PostMapping("/check-in")
    public ResponseEntity<VisitorCheckInResponse> checkIn(
            @RequestParam UUID tenantId,
            @RequestBody @Valid CheckInVisitorRequest request
    ) {


        VisitorCheckIn checkIn =
                service.checkIn(
                        tenantId,
                        request.visitorRequestId(),
                        request.gateLocation(),
                        request.badgeNumber(),
                        request.checkedInBy()
                );


        return ResponseEntity.ok(
                VisitorCheckInResponse.from(checkIn)
        );
    }


    @PostMapping("/{id}/check-out")
    public ResponseEntity<VisitorCheckInResponse> checkOut(
            @RequestParam UUID tenantId,
            @PathVariable UUID id,
            @RequestParam String checkedOutBy
    ) {


        VisitorCheckIn checkIn =
                service.checkOut(
                        tenantId,
                        id,
                        checkedOutBy
                );


        return ResponseEntity.ok(
                VisitorCheckInResponse.from(checkIn)
        );
    }


    @GetMapping("/active")
    public ResponseEntity<List<VisitorCheckInResponse>> activeVisitors(
            @RequestParam UUID tenantId
    ) {


        return ResponseEntity.ok(

                service.activeVisitors(tenantId)
                        .stream()
                        .map(VisitorCheckInResponse::from)
                        .toList()

        );
    }
}
