package africa.growtogether.platform.school.guardian;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/guardians")
public class GuardianController {

    private final GuardianService service;

    public GuardianController(
            GuardianService service
    ) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.guardian.create')"
    )
    public ResponseEntity<Guardian> create(
            @RequestParam UUID tenantId,
            @Valid @RequestBody CreateGuardianCommand command
    ) {

        return ResponseEntity.ok(
                service.create(
                        tenantId,
                        command
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('school.guardian.read')"
    )
    public ResponseEntity<Guardian> get(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.get(
                        tenantId,
                        id
                )
        );
    }

    @PatchMapping("/{id}/verification/pending")
    @PreAuthorize(
            "hasAuthority('school.guardian.manage')"
    )
    public ResponseEntity<Guardian> markVerificationPending(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.markVerificationPending(
                        tenantId,
                        id
                )
        );
    }

    @PatchMapping("/{id}/verification/verify")
    @PreAuthorize(
            "hasAuthority('school.guardian.manage')"
    )
    public ResponseEntity<Guardian> verify(
            @PathVariable UUID id,
            @RequestParam UUID tenantId,
            @RequestParam UUID verifiedBy
    ) {

        return ResponseEntity.ok(
                service.verify(
                        tenantId,
                        id,
                        verifiedBy
                )
        );
    }

    @PatchMapping("/{id}/restrict")
    @PreAuthorize(
            "hasAuthority('school.guardian.manage')"
    )
    public ResponseEntity<Guardian> restrict(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.restrict(
                        tenantId,
                        id
                )
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(
            "hasAuthority('school.guardian.manage')"
    )
    public ResponseEntity<Guardian> activate(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.activate(
                        tenantId,
                        id
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(
            "hasAuthority('school.guardian.manage')"
    )
    public ResponseEntity<Guardian> deactivate(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.deactivate(
                        tenantId,
                        id
                )
        );
    }
}
