package africa.growtogether.platform.school.profile;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/profiles")
public class SchoolProfileController {

    private final SchoolProfileService service;

    public SchoolProfileController(
            SchoolProfileService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SchoolProfile> create(
            @Valid @RequestBody CreateSchoolProfileCommand command
    ) {
        return ResponseEntity.ok(
                service.create(command)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolProfile> get(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                service.get(id)
        );
    }
}
