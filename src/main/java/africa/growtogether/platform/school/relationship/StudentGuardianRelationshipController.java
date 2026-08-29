package africa.growtogether.platform.school.relationship;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/student-guardian-relationships")
public class StudentGuardianRelationshipController {

    private final StudentGuardianRelationshipService service;

    public StudentGuardianRelationshipController(
            StudentGuardianRelationshipService service
    ) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.create')"
    )
    public ResponseEntity<StudentGuardianRelationship> create(
            @RequestParam UUID tenantId,
            @Valid @RequestBody CreateStudentGuardianRelationshipCommand command
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
            "hasAuthority('school.guardian-relationship.read')"
    )
    public ResponseEntity<StudentGuardianRelationship> get(
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

    @GetMapping("/student/{studentId}")
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.read')"
    )
    public ResponseEntity<List<StudentGuardianRelationship>> findByStudent(
            @PathVariable UUID studentId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByStudent(
                        tenantId,
                        studentId
                )
        );
    }

    @GetMapping("/guardian/{guardianId}")
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.read')"
    )
    public ResponseEntity<List<StudentGuardianRelationship>> findByGuardian(
            @PathVariable UUID guardianId,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.findByGuardian(
                        tenantId,
                        guardianId
                )
        );
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.manage')"
    )
    public ResponseEntity<StudentGuardianRelationship> suspend(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.suspend(
                        tenantId,
                        id
                )
        );
    }

    @PatchMapping("/{id}/restrict")
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.manage')"
    )
    public ResponseEntity<StudentGuardianRelationship> restrict(
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

    @PatchMapping("/{id}/end")
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.manage')"
    )
    public ResponseEntity<StudentGuardianRelationship> end(
            @PathVariable UUID id,
            @RequestParam UUID tenantId,
            @RequestParam LocalDate effectiveTo
    ) {

        return ResponseEntity.ok(
                service.end(
                        tenantId,
                        id,
                        effectiveTo
                )
        );
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize(
            "hasAuthority('school.guardian-relationship.manage')"
    )
    public ResponseEntity<StudentGuardianRelationship> reactivate(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        return ResponseEntity.ok(
                service.reactivate(
                        tenantId,
                        id
                )
        );
    }
}
