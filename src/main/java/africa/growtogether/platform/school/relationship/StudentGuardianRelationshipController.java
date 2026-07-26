package africa.growtogether.platform.school.relationship;

import jakarta.validation.Valid;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<StudentGuardianRelationship> create(
            @Valid @RequestBody CreateStudentGuardianRelationshipCommand command
    ) {

        return ResponseEntity.ok(
                service.create(command)
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<StudentGuardianRelationship> get(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                service.get(id)
        );
    }
}
