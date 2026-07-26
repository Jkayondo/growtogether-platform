package africa.growtogether.platform.school.learner;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Learner360View> getLearner360(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                service.get(id)
        );
    }
}
