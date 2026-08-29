package africa.growtogether.platform.school.learner;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school/learners")
public class Learner360Controller {

    private final Learner360Service service;

    public Learner360Controller(
            Learner360Service service
    ) {
        this.service = service;
    }

    @GetMapping("/{id}/360")
    @PreAuthorize(
            "hasAuthority('school.student.read')"
    )
    public ResponseEntity<Learner360View> getLearner360(
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
