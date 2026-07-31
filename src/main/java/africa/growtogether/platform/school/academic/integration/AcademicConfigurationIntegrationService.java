package africa.growtogether.platform.school.academic.integration;


import africa.growtogether.platform.school.academic.AcademicStructureService;
import africa.growtogether.platform.school.academic.curriculum.Curriculum;
import africa.growtogether.platform.school.academic.curriculum.CurriculumService;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class AcademicConfigurationIntegrationService {


    private final CurriculumService curriculumService;

    private final AcademicStructureService academicStructureService;


    public AcademicConfigurationIntegrationService(
            CurriculumService curriculumService,
            AcademicStructureService academicStructureService
    ) {

        this.curriculumService = curriculumService;
        this.academicStructureService = academicStructureService;
    }


    public Curriculum configureCurriculum(
            UUID tenantId,
            String curriculumCode,
            String curriculumName,
            String curriculumType
    ) {

        return curriculumService.create(
                tenantId,
                curriculumCode,
                curriculumName,
                curriculumType
        );
    }


    public void configureAcademicLevel(
            UUID tenantId,
            UUID curriculumConfigurationId,
            String levelName,
            Integer displayOrder
    ) {

        academicStructureService.createLevel(
                tenantId,
                curriculumConfigurationId,
                levelName,
                displayOrder
        );
    }


    public void configureAcademicGrade(
            UUID tenantId,
            UUID academicLevelId,
            String gradeName,
            Integer displayOrder
    ) {

        academicStructureService.createGrade(
                tenantId,
                academicLevelId,
                gradeName,
                displayOrder
        );
    }
}
