package africa.growtogether.platform.school.student;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/students")
public class StudentController {

    private final StudentService service;

    public StudentController(
            StudentService service
    ) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.student.create')"
    )
    public ResponseEntity<Student> create(
            @RequestParam UUID tenantId,
            @Valid @RequestBody CreateStudentCommand command
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
            "hasAuthority('school.student.read')"
    )
    public ResponseEntity<Student> get(
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
}
