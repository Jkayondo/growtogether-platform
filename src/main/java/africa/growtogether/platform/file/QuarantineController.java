package africa.growtogether.platform.file;


import africa.growtogether.platform.common.security.EnterpriseIdentityContext;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/v1/files/quarantine")
public class QuarantineController {


    private final QuarantineService service;

    private final EnterpriseIdentityContext identity;



    public QuarantineController(
            QuarantineService service,
            EnterpriseIdentityContext identity
    ) {

        this.service = service;
        this.identity = identity;

    }



    @GetMapping
    @PreAuthorize(
            "hasAuthority('file.quarantine.read')"
    )
    public ResponseEntity<List<QuarantinedFile>> pending() {


        UUID tenantId =
                identity.requireTenantId();


        return ResponseEntity.ok(
                service.pending(
                        tenantId
                )
        );

    }



    @PostMapping("/{id}/release")
    @PreAuthorize(
            "hasAuthority('file.quarantine.release')"
    )
    public ResponseEntity<QuarantinedFile> release(
            @PathVariable UUID id
    ) {


        return ResponseEntity.ok(
                service.release(
                        id
                )
        );

    }



    @PostMapping("/{id}/reject")
    @PreAuthorize(
            "hasAuthority('file.quarantine.reject')"
    )
    public ResponseEntity<QuarantinedFile> reject(
            @PathVariable UUID id,
            @RequestBody RejectRequest request
    ) {


        return ResponseEntity.ok(
                service.reject(
                        id,
                        request.reason()
                )
        );

    }



    public record RejectRequest(
            String reason
    ) {}

}
