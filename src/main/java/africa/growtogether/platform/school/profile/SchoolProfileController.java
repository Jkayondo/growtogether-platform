package africa.growtogether.platform.school.profile;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/profiles")
public class SchoolProfileController {

    private final SchoolProfileService service;
    private final EnterpriseIdentityContext identity;

    public SchoolProfileController(
            SchoolProfileService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }

    @PostMapping
    public ResponseEntity<SchoolProfile> create(
            @RequestParam UUID tenantId,
            @Valid @RequestBody CreateSchoolProfileCommand command
    ) {

        identity.requireTenant(
                tenantId
        );

        return ResponseEntity.ok(
                service.create(
                        tenantId,
                        command
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolProfile> get(
            @RequestParam UUID tenantId,
            @PathVariable UUID id
    ) {

        identity.requireTenant(
                tenantId
        );

        return ResponseEntity.ok(
                service.get(
                        tenantId,
                        id
                )
        );
    }

    @GetMapping("/current")
    public ResponseEntity<SchoolProfile> current(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(
                tenantId
        );

        return ResponseEntity.ok(
                service.getForTenant(
                        tenantId
                )
        );
    }
}
