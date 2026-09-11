package africa.growtogether.platform.school.student;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/students")
public class StudentController {

    private final StudentService service;
    private final EnterpriseIdentityContext identity;

    public StudentController(
            StudentService service,
            EnterpriseIdentityContext identity
    ) {
        this.service = service;
        this.identity = identity;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.student.create')"
    )
    public ResponseEntity<Student> create(
            @RequestParam UUID tenantId,
            @Valid @RequestBody CreateStudentCommand command
    ) {

        identity.requireTenant(tenantId);

        return ResponseEntity.ok(
                service.create(
                        tenantId,
                        command
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAuthority('school.student.read')"
    )
    public ResponseEntity<List<Student>> list(
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return ResponseEntity.ok(
                service.findActiveStudents(
                        tenantId
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('school.student.read')"
    )
    public ResponseEntity<Student> get(
            @PathVariable UUID id,
            @RequestParam UUID tenantId
    ) {

        identity.requireTenant(tenantId);

        return ResponseEntity.ok(
                service.get(
                        tenantId,
                        id
                )
        );
    }
}
