package africa.growtogether.platform.school.visitor.api;


import africa.growtogether.platform.school.visitor.api.dto.CreateVisitorRequest;
import africa.growtogether.platform.school.visitor.api.dto.VisitorResponse;
import africa.growtogether.platform.school.visitor.domain.SchoolVisitor;
import africa.growtogether.platform.school.visitor.service.VisitorManagementService;


import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/school/visitors")
public class VisitorManagementController {


    private final VisitorManagementService service;


    public VisitorManagementController(
            VisitorManagementService service
    ) {
        this.service = service;
    }


    @PostMapping
    public VisitorResponse create(
            @RequestParam UUID tenantId,
            @RequestBody CreateVisitorRequest request
    ) {

        SchoolVisitor visitor =
                service.registerVisitor(
                        tenantId,
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.email(),
                        request.identificationType(),
                        request.identificationReference(),
                        request.visitorCategory()
                );


        return map(visitor);
    }


    @GetMapping("/{id}")
    public VisitorResponse get(
            @RequestParam UUID tenantId,
            @PathVariable UUID id
    ) {

        return map(
                service.getVisitor(
                        tenantId,
                        id
                )
        );
    }


    @GetMapping
    public List<VisitorResponse> list(
            @RequestParam UUID tenantId
    ) {

        return service.listVisitors(tenantId)
                .stream()
                .map(this::map)
                .toList();
    }


    private VisitorResponse map(
            SchoolVisitor visitor
    ) {

        return new VisitorResponse(
                visitor.getId(),
                visitor.getFirstName(),
                visitor.getLastName(),
                visitor.getPhoneNumber(),
                visitor.getEmail(),
                visitor.getIdentificationType(),
                visitor.getIdentificationReference(),
                visitor.getVisitorCategory()
        );
    }
}
