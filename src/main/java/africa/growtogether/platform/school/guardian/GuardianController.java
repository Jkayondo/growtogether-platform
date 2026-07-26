package africa.growtogether.platform.school.guardian;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Guardian> create(
            @Valid @RequestBody CreateGuardianCommand command
    ) {
        return ResponseEntity.ok(
                service.create(command)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Guardian> get(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                service.get(id)
        );
    }
}
