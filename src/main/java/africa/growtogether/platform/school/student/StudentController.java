package africa.growtogether.platform.school.student;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Student> create(
            @Valid @RequestBody CreateStudentCommand command
    ) {
        return ResponseEntity.ok(
                service.create(command)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> get(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                service.get(id)
        );
    }
}
