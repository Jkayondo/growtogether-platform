package africa.growtogether.platform.school.academic.integration;


import africa.growtogether.platform.school.academic.curriculum.Curriculum;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/configuration")
public class AcademicConfigurationIntegrationController {


    private final AcademicConfigurationIntegrationService service;


    public AcademicConfigurationIntegrationController(
            AcademicConfigurationIntegrationService service
    ) {

        this.service = service;
    }


    @PostMapping("/curriculum")
    public ResponseEntity<Curriculum> configureCurriculum(
            @RequestParam UUID tenantId,
            @RequestParam String curriculumCode,
            @RequestParam String curriculumName,
            @RequestParam String curriculumType
    ) {

        return ResponseEntity.ok(
                service.configureCurriculum(
                        tenantId,
                        curriculumCode,
                        curriculumName,
                        curriculumType
                )
        );
    }


    @PostMapping("/level")
    public ResponseEntity<Void> configureLevel(
            @RequestParam UUID tenantId,
            @RequestParam UUID curriculumConfigurationId,
            @RequestParam String levelName,
            @RequestParam Integer displayOrder
    ) {

        service.configureAcademicLevel(
                tenantId,
                curriculumConfigurationId,
                levelName,
                displayOrder
        );

        return ResponseEntity.ok().build();
    }


    @PostMapping("/grade")
    public ResponseEntity<Void> configureGrade(
            @RequestParam UUID tenantId,
            @RequestParam UUID academicLevelId,
            @RequestParam String gradeName,
            @RequestParam Integer displayOrder
    ) {

        service.configureAcademicGrade(
                tenantId,
                academicLevelId,
                gradeName,
                displayOrder
        );

        return ResponseEntity.ok().build();
    }
}
