package africa.growtogether.platform.school.assessment.examination;


import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/school/examination-sessions")
public class ExaminationSessionController {


    private final ExaminationSessionService service;


    public ExaminationSessionController(
            ExaminationSessionService service
    ) {

        this.service = service;

    }


    @PostMapping
    public ExaminationSession create(
            @RequestBody ExaminationSession session
    ) {

        return service.create(session);

    }


    @GetMapping("/{id}")
    public ExaminationSession get(
            @PathVariable UUID id
    ) {

        return service.get(id);

    }


    @GetMapping("/code/{sessionCode}")
    public ExaminationSession getByCode(
            @RequestParam UUID tenantId,
            @PathVariable String sessionCode
    ) {

        return service.getByCode(
                tenantId,
                sessionCode
        );

    }


    @PostMapping("/{id}/approve")
    public ExaminationSession approve(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy
    ) {

        return service.approve(
                id,
                approvedBy
        );

    }


    @PostMapping("/{id}/open-registration")
    public ExaminationSession openRegistration(
            @PathVariable UUID id
    ) {

        return service.openRegistration(id);

    }


    @PostMapping("/{id}/activate")
    public ExaminationSession activate(
            @PathVariable UUID id
    ) {

        return service.activate(id);

    }


    @PostMapping("/{id}/complete")
    public ExaminationSession complete(
            @PathVariable UUID id
    ) {

        return service.complete(id);

    }

}
