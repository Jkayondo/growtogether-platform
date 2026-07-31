package africa.growtogether.platform.school.lifecycle;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/lifecycle")
public class SchoolLifecycleController {


    private final SchoolLifecycleService service;


    public SchoolLifecycleController(
            SchoolLifecycleService service
    ) {

        this.service = service;
    }


    @PostMapping("/{schoolConfigurationId}/start")
    public ResponseEntity<Void> start(
            @PathVariable UUID schoolConfigurationId,
            @RequestParam UUID tenantId
    ) {


        service.startSchoolLifecycle(
                tenantId,
                schoolConfigurationId
        );


        return ResponseEntity.ok().build();
    }
}
